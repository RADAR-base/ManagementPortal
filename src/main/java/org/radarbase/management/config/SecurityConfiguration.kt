package org.radarbase.management.config

import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.jwks.JwkAlgorithmParser
import org.radarbase.auth.jwks.JwksTokenVerifierLoader
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.Http401UnauthorizedEntryPoint
import org.radarbase.management.security.JwtAuthenticationFilter
import org.radarbase.management.security.RadarAuthenticationProvider
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import tech.jhipster.security.AjaxLogoutSuccessHandler
import javax.annotation.PostConstruct

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfiguration
/** Security configuration constructor. */
    @Autowired constructor(
        private val authenticationManagerBuilder: AuthenticationManagerBuilder,
        private val userDetailsService: UserDetailsService,
        private val applicationEventPublisher: ApplicationEventPublisher,
        private val passwordEncoder: PasswordEncoder,
        private val managementPortalProperties: ManagementPortalProperties,
        private val userRepository: UserRepository
    ) : WebSecurityConfigurerAdapter() {
        val tokenValidator: TokenValidator
            /** Get the default token validator. */
            get() {
                val loaderList =
                    listOf(
                        JwksTokenVerifierLoader(
                            managementPortalProperties.authServer.serverAdminUrl +
                                "/admin/keys/hydra.jwt.access-token",
                            RES_MANAGEMENT_PORTAL,
                            JwkAlgorithmParser(),
                        ),
                        JwksTokenVerifierLoader(
                            managementPortalProperties.common.managementPortalBaseUrl +"/oauth/token_key",
                            RES_MANAGEMENT_PORTAL,
                            JwkAlgorithmParser()
                        )
                    )
                return TokenValidator(loaderList)
            }

        @PostConstruct
        fun init() {
            try {
                authenticationManagerBuilder
                    .userDetailsService(userDetailsService) // Legacy username/password authentication
                    .passwordEncoder(passwordEncoder)
                    .and()
                    .authenticationProvider(RadarAuthenticationProvider()) // Internal authentication provider
                    .authenticationEventPublisher(DefaultAuthenticationEventPublisher(applicationEventPublisher))
            } catch (e: Exception) {
                throw BeanInitializationException("Security configuration failed", e)
            }
        }

        @Bean
        fun logoutSuccessHandler(): LogoutSuccessHandler = AjaxLogoutSuccessHandler()

        @Bean
        fun http401UnauthorizedEntryPoint(): Http401UnauthorizedEntryPoint = Http401UnauthorizedEntryPoint()

        @Bean
        fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
            val useInternalAuth = managementPortalProperties.authServer.internal

            return JwtAuthenticationFilter(
                validator = tokenValidator,
                authenticationManager = authenticationManager(),
                enableUserLookup = useInternalAuth,
                userRepository = if (useInternalAuth) userRepository else null,
                isOptional = false)
                .skipUrlPattern(HttpMethod.GET, "/")
                .skipUrlPattern(HttpMethod.GET, "/*.{js,ico,css,html}")
                .skipUrlPattern(HttpMethod.GET, "/i18n/**")
                .skipUrlPattern(HttpMethod.GET, "/management/health")
                .skipUrlPattern(HttpMethod.POST, "/oauth/token")
                .skipUrlPattern(HttpMethod.GET, "/oauth/token_key")
                .skipUrlPattern(HttpMethod.GET, "/oauth/authorize")
                .skipUrlPattern(HttpMethod.POST, "/oauth/authorize")
                .skipUrlPattern(HttpMethod.GET, "/oauth/confirm_access")
                .skipUrlPattern(HttpMethod.POST, "/oauth/confirm_access")
                .skipUrlPattern(HttpMethod.GET, "/api/meta-token/*")
                .skipUrlPattern(HttpMethod.GET, "/api/public/projects")
                .skipUrlPattern(HttpMethod.GET, "/api/sitesettings")
                .skipUrlPattern(HttpMethod.GET, "/api/redirect/**")
                .skipUrlPattern(HttpMethod.GET, "/api/profile-info")
                .skipUrlPattern(HttpMethod.GET, "/api/logout-url")
                .skipUrlPattern(HttpMethod.POST, "/api/webhook/**")
                .skipUrlPattern(HttpMethod.GET, "/oauth2/authorize")
                .skipUrlPattern(HttpMethod.GET, "/images/**")
                .skipUrlPattern(HttpMethod.GET, "/css/**")
                .skipUrlPattern(HttpMethod.GET, "/js/**")
                .skipUrlPattern(HttpMethod.GET, "/radar-baseRR.png")
                .skipUrlPattern(HttpMethod.GET, "/login")
                .skipUrlPattern(HttpMethod.POST, "/login")
        }

        override fun configure(web: WebSecurity) {
            web.ignoring()
                .antMatchers("/")
                .antMatchers("/*.{js,ico,css,html}")
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/bower_components/**")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/**")
                .antMatchers("/api-docs/**")
                .antMatchers("/swagger-ui.html")
                .antMatchers("/api-docs{,.json,.yml}")
                .antMatchers("/api/logout-url")
                .antMatchers("/api/profile-info")
                .antMatchers("/api/activate")
                .antMatchers("/api/sitesettings")
                .antMatchers("/api/redirect/**")
                .antMatchers("/api/webhook/**")
                .antMatchers("/api/register")
                .antMatchers("/api/account/reset_password/init")
                .antMatchers("/api/account/reset_password/finish")
                .antMatchers("/test/**")
                .antMatchers("/management/health")
                .antMatchers(HttpMethod.GET, "/api/meta-token/**")
        }

        @Throws(Exception::class)
        public override fun configure(http: HttpSecurity) {
            http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(http401UnauthorizedEntryPoint())
                .and()
                .addFilterBefore(
                    jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter::class.java,
                )
                .authorizeRequests()
                .anyRequest().authenticated() // Allow all requests if authenticated
                .and()
                .logout().invalidateHttpSession(true)
                .logoutUrl("/api/logout")
        }

        @Bean
        @Throws(Exception::class)
        override fun authenticationManagerBean(): AuthenticationManager {
            return super.authenticationManagerBean()
        }

        @Bean
        fun securityEvaluationContextExtension(): SecurityEvaluationContextExtension {
            return SecurityEvaluationContextExtension()
        }

        companion object {
            const val RES_MANAGEMENT_PORTAL = "res_ManagementPortal"
        }
}
