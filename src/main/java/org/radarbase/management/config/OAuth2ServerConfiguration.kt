package org.radarbase.management.config

import javax.sql.DataSource
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.*
import org.radarbase.management.security.jwt.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.orm.jpa.vendor.Database
import org.springframework.security.authentication.*
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.*
import org.springframework.security.oauth2.config.annotation.web.configurers.*
import org.springframework.security.oauth2.provider.approval.*
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.springframework.security.oauth2.provider.code.*
import org.springframework.security.oauth2.provider.token.*
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

@ConditionalOnProperty(
    name = ["managementportal.authServer.internal"],
    havingValue = "true",
    matchIfMissing = true
)
@Configuration
class OAuth2ServerConfiguration(
    @Autowired private val dataSource: DataSource,
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    @Configuration
    @Order(-20)
    protected class LoginConfig(
        @Autowired private val authenticationManager: AuthenticationManager,
    ) : WebSecurityConfigurerAdapter() {

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            http
                .formLogin().loginPage("/login").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/token").permitAll()
                .and()
                .requestMatchers()
                .antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access")
                .and()
                .authorizeRequests().anyRequest().authenticated()
        }

        @Throws(Exception::class)
        override fun configure(auth: AuthenticationManagerBuilder) {
            auth.parentAuthenticationManager(authenticationManager)
        }
    }

    @Configuration
    class JwtAuthenticationFilterConfiguration(
        @Autowired private val authenticationManager: AuthenticationManager,
        @Autowired private val userRepository: UserRepository,
        @Autowired private val keyStoreHandler: ManagementPortalOauthKeyStoreHandler
    ) {
        @Bean
        fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
            return JwtAuthenticationFilter(
                keyStoreHandler.tokenValidator,
                authenticationManager,
                true,
                userRepository,
                true
            )
        }
    }

    @Bean
    fun jdbcClientDetailsService(): JdbcClientDetailsService {
        val clientDetailsService = JdbcClientDetailsService(dataSource)
        clientDetailsService.setPasswordEncoder(passwordEncoder)
        return clientDetailsService
    }

    @Configuration
    @EnableResourceServer
    protected class ResourceServerConfiguration(
        @Autowired private val tokenStore: TokenStore,
        @Autowired private val http401UnauthorizedEntryPoint: Http401UnauthorizedEntryPoint,
        @Autowired private val logoutSuccessHandler: LogoutSuccessHandler,
        @Autowired private val jwtAuthenticationFilter: JwtAuthenticationFilter
    ) : ResourceServerConfigurerAdapter() {

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            http
                .exceptionHandling()
                .authenticationEntryPoint(http401UnauthorizedEntryPoint)
                .and()
                .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter::class.java
                )
                .authorizeRequests()
                .antMatchers("/oauth/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/register").hasAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/sitesettings").permitAll()
                .antMatchers("/api/public/projects").permitAll()
                .antMatchers("/api/logout-url").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/index.html").hasAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .and()
                .logout().invalidateHttpSession(true)
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .headers().frameOptions().disable()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        }

        @Throws(Exception::class)
        override fun configure(resources: ResourceServerSecurityConfigurer) {
            resources.resourceId("res_ManagementPortal")
                .tokenStore(tokenStore)
                .eventPublisher(CustomEventPublisher())
        }

        protected class CustomEventPublisher : DefaultAuthenticationEventPublisher() {
            override fun publishAuthenticationSuccess(authentication: Authentication) {
                // Suppress OAuth2 audit spam
            }
        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected class AuthorizationServerConfiguration(
        @Autowired private val jpaProperties: JpaProperties,
        @Autowired @Qualifier("authenticationManagerBean") private val authenticationManager: AuthenticationManager,
        @Autowired private val dataSource: DataSource,
        @Autowired private val jdbcClientDetailsService: JdbcClientDetailsService,
        @Autowired private val keyStoreHandler: ManagementPortalOauthKeyStoreHandler
    ) : AuthorizationServerConfigurerAdapter() {

        @Bean
        protected fun authorizationCodeServices(): AuthorizationCodeServices =
            JdbcAuthorizationCodeServices(dataSource)

        @Bean
        fun approvalStore(): ApprovalStore =
            if (jpaProperties.database == Database.POSTGRESQL)
                PostgresApprovalStore(dataSource)
            else
                JdbcApprovalStore(dataSource)

        @Bean
        fun tokenEnhancer(): TokenEnhancer = ClaimsTokenEnhancer()

        @Bean
        fun tokenStore(): TokenStore =
            ManagementPortalJwtTokenStore(accessTokenConverter())

        @Bean
        fun accessTokenConverter(): ManagementPortalJwtAccessTokenConverter {
            logger.debug("loading token converter from keystore configurations")
            return ManagementPortalJwtAccessTokenConverter(
                keyStoreHandler.algorithmForSigning,
                keyStoreHandler.verifiers,
                keyStoreHandler.refreshTokenVerifiers
            )
        }

        @Bean
        @Primary
        fun tokenServices(tokenStore: TokenStore?): DefaultTokenServices {
            return DefaultTokenServices().apply {
                setTokenStore(tokenStore)
                setSupportRefreshToken(true)
                setReuseRefreshToken(false)
            }
        }

        override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
            val tokenEnhancerChain = TokenEnhancerChain().apply {
                setTokenEnhancers(listOf(tokenEnhancer(), accessTokenConverter()))
            }
            endpoints
                .authorizationCodeServices(authorizationCodeServices())
                .approvalStore(approvalStore())
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .reuseRefreshTokens(false)
                .authenticationManager(authenticationManager)
        }

        override fun configure(oauthServer: AuthorizationServerSecurityConfigurer) {
            oauthServer
                .allowFormAuthenticationForClients()
                .checkTokenAccess("isAuthenticated()")
                .tokenKeyAccess("permitAll()")
                .passwordEncoder(BCryptPasswordEncoder())
        }

        override fun configure(clients: ClientDetailsServiceConfigurer) {
            clients.withClientDetails(jdbcClientDetailsService)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OAuth2ServerConfiguration::class.java)
    }
}
