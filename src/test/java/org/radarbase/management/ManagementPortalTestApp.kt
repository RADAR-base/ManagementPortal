package org.radarbase.management

import org.radarbase.management.containers.KratosContainer
import org.radarbase.management.config.ApplicationProperties
import org.radarbase.management.config.KratosProperties
import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import tech.jhipster.config.JHipsterConstants
import java.net.InetAddress
import java.net.UnknownHostException
import javax.annotation.PostConstruct

/**
 * This is the application configuration that excludes CommandLineRunner(i.e the sourceTypeLoader).
 * This is used for testing to replicate the application setup without SourceTypeLoader.
 */
@ComponentScan(
    "org.radarbase.management.config",
    "org.radarbase.management.domain.support",
    "org.radarbase.management.filters",
    "org.radarbase.management.repository",
    "org.radarbase.management.service",
    "org.radarbase.management.security",
    "org.radarbase.management.web"
)
@EnableAutoConfiguration
@EnableConfigurationProperties(
    LiquibaseProperties::class,
    ApplicationProperties::class,
    ManagementPortalProperties::class,
    KratosProperties::class
)
class ManagementPortalTestApp(private val env: Environment) {
    private val kratosContainer = KratosContainer()

    /**
     * Initializes ManagementPortal.
     *
     *
     * Spring profiles can be configured with a program arguments
     * --spring.profiles.active=your-active-profile
     *
     *
     * You can find more information on how profiles work with JHipster on
     * [
 * http://jhipster.github.io/profiles/
](http://jhipster.github.io/profiles/) * .
     */
    @PostConstruct
    fun initApplication() {
       if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))
        && env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))
    ) {
        log.error(
            "You have misconfigured your application! It should not run "
                    + "with both the 'dev' and 'prod' profiles at the same time."
        )
    }
    if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))
        && env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_CLOUD))
    ) {
        log.error(
            "You have misconfigured your application! It should not"
                    + "run with both the 'dev' and 'cloud' profiles at the same time."
        )
    }

    kratosContainer.start()

    val kratosPublicPort = kratosContainer.getPublicUrl()
    val kratosAdminPort = kratosContainer.getAdminUrl()

    log.info("KratosContainer started:")
    log.info("Public API at: $kratosPublicPort")
    log.info("Admin API at:  $kratosAdminPort")

    System.setProperty("kratos.public-url", kratosContainer.getPublicUrl())
    System.setProperty("kratos.admin-url", kratosContainer.getAdminUrl())
    }

    companion object {
        private val log = LoggerFactory.getLogger(ManagementPortalTestApp::class.java)

        /**
         * Main method, used to run the application.
         *
         * @param args the command line arguments
         * @throws UnknownHostException if the local host name could not be resolved into an address
         */
        @Throws(UnknownHostException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(ManagementPortalTestApp::class.java)
            val env: Environment = app.run(*args).environment
            var protocol = "http"
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https"
            }
            log.info(
                "\n--------------------------------------------------\n\t"
                        + "Application '{}' is running! Access URLs:\n\t"
                        + "Local: \t\t{}://localhost:{}\n\t"
                        + "External: \t{}://{}:{}\n\t"
                        + "Profile(s): \t{}\n--------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().hostAddress,
                env.getProperty("server.port"),
                env.activeProfiles
            )
        }
    }
}
