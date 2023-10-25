package org.radarbase.management.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.nio.charset.StandardCharsets

@Configuration
open class ThymeleafConfiguration {
    @Bean
    @Description("Thymeleaf template resolver serving HTML 5 emails")
    open fun emailTemplateResolver(): ClassLoaderTemplateResolver {
        val emailTemplateResolver = ClassLoaderTemplateResolver()
        emailTemplateResolver.prefix = "templates/"
        emailTemplateResolver.suffix = ".html"
        emailTemplateResolver.setTemplateMode("HTML")
        emailTemplateResolver.characterEncoding = StandardCharsets.UTF_8.name()
        emailTemplateResolver.order = 1
        return emailTemplateResolver
    }
}
