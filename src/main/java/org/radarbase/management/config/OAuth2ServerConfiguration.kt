package org.radarbase.management.config

import java.util.*
import javax.sql.DataSource
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.ClaimsTokenEnhancer
import org.radarbase.management.security.Http401UnauthorizedEntryPoint
import org.radarbase.management.security.JwtAuthenticationFilter
import org.radarbase.management.security.PostgresApprovalStore
import org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter
import org.radarbase.management.security.jwt.ManagementPortalJwtTokenStore
import org.radarbase.management.security.jwt.ManagementPortalOauthKeyStoreHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.orm.jpa.vendor.Database
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.approval.ApprovalStore
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenEnhancer
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

@Configuration
class OAuth2ServerConfiguration(
    @Autowired private val dataSource: DataSource,
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    @Configuration
    @Order(-20)
    protected class LoginConfig(
        @Autowired private val authenticationManager: AuthenticationManager,
        @Autowired private val jwtAuthenticationFilter: JwtAuthenticationFilter
    ) : WebSecurityConfigurerAdapter() {

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            http
                .formLogin().loginPage("/login").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/token").permitAll()
                .and()
                .addFilterAfter(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter::class.java
                )
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
        @Autowired private val keyStoreHandler: ManagementPortalOauthKeyStoreHandler,
        @Autowired private val tokenStore: TokenStore,
        @Autowired private val http401UnauthorizedEntryPoint: Http401UnauthorizedEntryPoint,
        @Autowired private val logoutSuccessHandler: LogoutSuccessHandler,
        @Autowired private val authenticationManager: AuthenticationManager,
        @Autowired private val userRepository: UserRepository
    ) : ResourceServerConfigurerAdapter() {

        fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
            return JwtAuthenticationFilter(
                keyStoreHandler.tokenValidator, authenticationManager, userRepository
            )
                .skipUrlPattern(HttpMethod.GET, "/management/health")
                .skipUrlPattern(HttpMethod.POST, "/oauth/token")
                .skipUrlPattern(HttpMethod.GET, "/api/meta-token/*")
                .skipUrlPattern(HttpMethod.GET, "/api/public/projects")
                .skipUrlPattern(HttpMethod.GET, "/api/sitesettings")
                .skipUrlPattern(HttpMethod.GET, "/api/redirect/**")
                .skipUrlPattern(HttpMethod.GET, "/api/logout-url")
                .skipUrlPattern(HttpMethod.GET, "/oauth2/authorize")
                .skipUrlPattern(HttpMethod.GET, "/images/**")
                .skipUrlPattern(HttpMethod.GET, "/css/**")
                .skipUrlPattern(HttpMethod.GET, "/js/**")
                .skipUrlPattern(HttpMethod.GET, "/radar-baseRR.png")
        }

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            http
                .exceptionHandling()
                .authenticationEntryPoint(http401UnauthorizedEntryPoint)
                .and()
                .addFilterBefore(
                    jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter::class.java
                )
                .authorizeRequests()
                .antMatchers("/oauth/**").permitAll()
                .and()
                .logout().invalidateHttpSession(true)
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
                .addFilterBefore(
                    jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter::class.java
                )
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/register")
                .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/sitesettings").permitAll()
                .antMatchers("/api/public/projects").permitAll()
                .antMatchers("/api/logout-url").permitAll()

                .antMatchers("/api/**")
                .authenticated() // Allow management/health endpoint to all to allow kubernetes to be able to
                // detect the health of the service
                .antMatchers("/oauth/token").permitAll()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**")
                .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/index.html")
                .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
        }

        @Throws(Exception::class)
        override fun configure(resources: ResourceServerSecurityConfigurer) {
            resources.resourceId("res_ManagementPortal")
                .tokenStore(tokenStore)
                .eventPublisher(CustomEventPublisher())
        }

        protected class CustomEventPublisher : DefaultAuthenticationEventPublisher() {
            override fun publishAuthenticationSuccess(authentication: Authentication) {
                // OAuth2AuthenticationProcessingFilter publishes an authentication success audit
                // event for EVERY successful OAuth request to our API resources, this is way too
                // much so we override the event publisher to not publish these events.
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
        protected fun authorizationCodeServices(): AuthorizationCodeServices {
            return JdbcAuthorizationCodeServices(dataSource)
        }

        @Bean
        fun approvalStore(): ApprovalStore {
            return if (jpaProperties.database == Database.POSTGRESQL) {
                PostgresApprovalStore(dataSource)
            } else {
                // to have compatibility for other databases including H2
                JdbcApprovalStore(dataSource)
            }
        }

        @Bean
        fun tokenEnhancer(): TokenEnhancer {
            return ClaimsTokenEnhancer()
        }

        @Bean
        fun tokenStore(): TokenStore {
            return ManagementPortalJwtTokenStore(accessTokenConverter())
        }

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
            val defaultTokenServices = DefaultTokenServices()
            defaultTokenServices.setTokenStore(tokenStore)
            defaultTokenServices.setSupportRefreshToken(true)
            defaultTokenServices.setReuseRefreshToken(false)
            return defaultTokenServices
        }

        override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
            val tokenEnhancerChain = TokenEnhancerChain()
            tokenEnhancerChain.setTokenEnhancers(
                listOf(tokenEnhancer(), accessTokenConverter())
            )
            endpoints
                .authorizationCodeServices(authorizationCodeServices())
                .approvalStore(approvalStore())
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .reuseRefreshTokens(false)
                .authenticationManager(authenticationManager)
        }

        override fun configure(oauthServer: AuthorizationServerSecurityConfigurer) {
            oauthServer.allowFormAuthenticationForClients()
                .checkTokenAccess("isAuthenticated()")
                .tokenKeyAccess("permitAll()")
                .passwordEncoder(BCryptPasswordEncoder())
        }

        @Throws(Exception::class)
        override fun configure(clients: ClientDetailsServiceConfigurer) {
            clients.withClientDetails(jdbcClientDetailsService)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OAuth2ServerConfiguration::class.java)
    }
}
