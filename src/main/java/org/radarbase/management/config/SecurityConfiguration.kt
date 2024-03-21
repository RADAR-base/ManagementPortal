package org.radarbase.management.config

import org.radarbase.management.security.Http401UnauthorizedEntryPoint
import org.radarbase.management.security.RadarAuthenticationProvider
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import tech.jhipster.security.AjaxLogoutSuccessHandler


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfiguration {

    @Autowired
    fun buildAuthenticationManager(
        authenticationManagerBuilder: AuthenticationManagerBuilder, passwordEncoder: PasswordEncoder?,
        applicationEventPublisher: ApplicationEventPublisher?, userDetailsService: UserDetailsService
    ) {
        try {
            authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
            authenticationManagerBuilder.authenticationProvider(RadarAuthenticationProvider())
            authenticationManagerBuilder.authenticationEventPublisher(
                DefaultAuthenticationEventPublisher(applicationEventPublisher)
            )
        } catch (e: java.lang.Exception) {
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

    @Bean
    @Throws(java.lang.Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // formatter:off
        http {
            authorizeHttpRequests {
                authorize("/", permitAll)
                authorize("/*.{js,ico,css,html}", permitAll)
                authorize("/app/**/*.{js,html}", permitAll)
                authorize("/bower_components/**", permitAll)
                authorize("/i18n/**", permitAll)
                authorize("/content/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/api-docs{,.json,.yml}", permitAll)
                authorize("/api/register", permitAll)
                authorize("/api/profile-info", permitAll)
                authorize("/api/activate", permitAll)
                authorize("/api/account/reset_password/init", permitAll)
                authorize("/api/account/reset_password/finish", permitAll)
                authorize("/test/**", permitAll)
                authorize("/management/health", permitAll)
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                authorize(HttpMethod.GET, "/api/meta-token/**", permitAll)
            }
            httpBasic {
                realmName = "ManagementPortal"
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED
            }
        }
        return http.build()
        // formatter:on
    }

    @Bean
    fun securityEvaluationContextExtension(): SecurityEvaluationContextExtension {
        return SecurityEvaluationContextExtension()
    }
}
