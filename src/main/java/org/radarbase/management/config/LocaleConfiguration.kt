package org.radarbase.management.config

import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import tech.jhipster.config.locale.AngularCookieLocaleResolver

@Configuration
class LocaleConfiguration :
    WebMvcConfigurer,
    EnvironmentAware {
    override fun setEnvironment(environment: Environment) {
        // unused
    }

    @Bean(name = ["localeResolver"])
    fun localeResolver(): LocaleResolver {
        val cookieLocaleResolver = AngularCookieLocaleResolver()
        cookieLocaleResolver.cookieName = "NG_TRANSLATE_LANG_KEY"
        return cookieLocaleResolver
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val localeChangeInterceptor = LocaleChangeInterceptor()
        localeChangeInterceptor.paramName = "language"
        registry.addInterceptor(localeChangeInterceptor)
    }
}
