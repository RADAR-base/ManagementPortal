package org.radarbase.management.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.MimeMappings
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import tech.jhipster.config.JHipsterConstants
import tech.jhipster.config.JHipsterProperties
import tech.jhipster.web.filter.CachingHttpHeadersFilter
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContext

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer : ServletContextInitializer, WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Autowired
    private val env: Environment? = null

    @Autowired
    private val jHipsterProperties: JHipsterProperties? = null
    override fun onStartup(servletContext: ServletContext) {
        if (env!!.activeProfiles.size != 0) {
            log.info(
                "Web application configuration, using profiles: {}",
                Arrays.asList(*env.activeProfiles)
            )
        }
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))) {
            val disps = EnumSet
                .of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC)
            initCachingHttpHeadersFilter(servletContext, disps)
        }
        log.info("Web application fully configured")
    }

    /**
     * Customize the Servlet engine: Mime types, the document root, the cache.
     */
    override fun customize(factory: ConfigurableServletWebServerFactory) {
        val mappings = MimeMappings(MimeMappings.DEFAULT)
        // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
        mappings.add("html", "text/html;charset=utf-8")
        // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
        mappings.add("json", "text/html;charset=utf-8")
        factory.setMimeMappings(mappings)
        // When running in an IDE or with ./gradlew bootRun, set location of the static web assets.
        setLocationForStaticAssets(factory)
    }

    private fun setLocationForStaticAssets(factory: ConfigurableServletWebServerFactory) {
        val root: File
        val prefixPath = resolvePathPrefix()
        root = File(prefixPath + "build/www/")
        if (root.exists() && root.isDirectory()) {
            factory.setDocumentRoot(root)
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private fun resolvePathPrefix(): String {
        val fullExecutablePath = this.javaClass.getResource("").path
        val rootPath = Paths.get(".").toUri().normalize().getPath()
        val extractedPath = fullExecutablePath.replace(rootPath, "")
        val extractionEndIndex = extractedPath.indexOf("build/")
        return if (extractionEndIndex <= 0) {
            ""
        } else extractedPath.substring(0, extractionEndIndex)
    }

    /**
     * Initializes the caching HTTP Headers Filter.
     */
    private fun initCachingHttpHeadersFilter(
        servletContext: ServletContext,
        disps: EnumSet<DispatcherType>
    ) {
        log.debug("Registering Caching HTTP Headers Filter")
        val cachingHttpHeadersFilter = servletContext.addFilter(
            "cachingHttpHeadersFilter",
            CachingHttpHeadersFilter(jHipsterProperties)
        )
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/content/*")
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/app/*")
        cachingHttpHeadersFilter.setAsyncSupported(true)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebConfigurer::class.java)
    }
}
