package org.radarbase.management.config

import javax.sql.DataSource
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.*
import org.radarbase.management.security.jwt.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
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
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import java.net.URI
import java.net.URISyntaxException

@AuthServerEnabled
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
                .requestMatchers()
                .antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access", "/css/**", "/images/**", "/js/**")
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/css/**", "/images/**", "/js/**").permitAll()
                .antMatchers("/oauth/authorize", "/oauth/confirm_access").authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler { request, response, authentication ->
                    request.session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext())
                    val savedRequest = request.session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") as? SavedRequest
                    val redirectUrl = if (savedRequest != null) {
                        savedRequest.redirectUrl
                    } else {
                        request.requestURI + if (request.queryString != null) "?${request.queryString}" else ""
                    }

                    val safeRedirectUrl = try {
                        val uri = URI(redirectUrl)
                        when {
                            // Allow relative URLs
                            !uri.isAbsolute && (uri.path.startsWith("/") || uri.path.startsWith(request.contextPath)) -> {
                                redirectUrl
                            }
                            // Allow absolute URLs from same host (fixes your issue)
                            uri.isAbsolute && uri.host == request.serverName && uri.scheme == request.scheme -> {
                                redirectUrl
                            }
                            // Default fallback
                            else -> {
                                logger.warn("Unsafe redirect URL blocked: $redirectUrl")
                                "/"
                            }
                        }
                    } catch (e: URISyntaxException) {
                        logger.warn("Invalid redirect URL: $redirectUrl", e)
                        "/"
                    }

                    response.sendRedirect(safeRedirectUrl)
                }
                .permitAll()
                .and()
                .csrf().disable()  // Disable CSRF for OAuth endpoints
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        }

        @Throws(Exception::class)
        override fun configure(auth: AuthenticationManagerBuilder) {
            auth.parentAuthenticationManager(authenticationManager)
        }
    }

    @AuthServerEnabled
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

    @AuthServerEnabled
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
                .requestMatchers()
                .antMatchers("/api/**", "/management/**")
                .and()
                .authorizeRequests()
                .antMatchers("/api/register").hasAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/sitesettings").permitAll()
                .antMatchers("/api/public/projects").permitAll()
                .antMatchers("/api/logout-url").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(http401UnauthorizedEntryPoint)
                .and()
                .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter::class.java
                )
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .logout()
                .invalidateHttpSession(true)
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
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

    @AuthServerEnabled
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
