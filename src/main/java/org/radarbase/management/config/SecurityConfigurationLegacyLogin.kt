package org.radarbase.management.config

import org.radarbase.management.security.Http401UnauthorizedEntryPoint
import org.radarbase.management.security.RadarAuthenticationProvider
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
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
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import tech.jhipster.security.AjaxLogoutSuccessHandler
import javax.annotation.PostConstruct

@Profile("legacy-login")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfigurationLegacyLogin
/** Security configuration constructor.  */ @Autowired constructor(
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val userDetailsService: UserDetailsService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val passwordEncoder: PasswordEncoder
) : WebSecurityConfigurerAdapter() {
    @PostConstruct
    fun init() {
        try {
            authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .authenticationProvider(RadarAuthenticationProvider())
                .authenticationEventPublisher(
                    DefaultAuthenticationEventPublisher(applicationEventPublisher)
                )
        } catch (e: Exception) {
            throw BeanInitializationException("Security configuration failed", e)
        }
    }

    @Bean
    fun logoutSuccessHandler(): LogoutSuccessHandler {
        return AjaxLogoutSuccessHandler()
    }

    @Bean
    fun http401UnauthorizedEntryPoint(): Http401UnauthorizedEntryPoint {
        return Http401UnauthorizedEntryPoint()
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
            .antMatchers("/api/register")
            .antMatchers("/api/profile-info")
            .antMatchers("/api/activate")
            .antMatchers("/api/account/reset_password/init")
            .antMatchers("/api/account/reset_password/finish")
            .antMatchers("/test/**")
            .antMatchers("/management/health")
            .antMatchers(HttpMethod.GET, "/api/meta-token/**")
    }

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {
        http
            .httpBasic().realmName("ManagementPortal")
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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
}
