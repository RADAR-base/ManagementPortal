package org.radarbase.management;

import tech.jhipster.config.DefaultProfileUtil;
import tech.jhipster.config.JHipsterConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import org.radarbase.management.config.ApplicationProperties;
import org.radarbase.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@ComponentScan({
        "org.radarbase.management.config",
        "org.radarbase.management.domain.support",
        "org.radarbase.management.filters",
        "org.radarbase.management.repository",
        "org.radarbase.management.service",
        "org.radarbase.management.security",
        "org.radarbase.management.web"
})
@EnableAutoConfiguration
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class,
        ManagementPortalProperties.class})
public class ManagementPortalApp {

    private static final Logger log = LoggerFactory.getLogger(ManagementPortalApp.class);

    private final Environment env;

    public ManagementPortalApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes ManagementPortal.
     *
     * <p>Spring profiles can be configured with a program arguments
     * --spring.profiles.active=your-active-profile</p>
     *
     * <p>You can find more information on how profiles work with JHipster on
     * <a href="http://jhipster.github.io/profiles/">
     *     http://jhipster.github.io/profiles/
     * </a></p>.
     */
    @PostConstruct
    public void initApplication() {
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))
                && env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))) {
            log.error("You have misconfigured your application! It should not run "
                    + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))
                && env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_CLOUD))) {
            log.error("You have misconfigured your application! It should not"
                    + "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(ManagementPortalApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("""

                        -----------------------------------------------------
                        \tApplication '{}' is running! Access URLs:
                        \tLocal: \t\t{}://localhost:{}
                        \tExternal: \t{}://{}:{}
                        \tProfile(s): \t{}
                        -----------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }
}
