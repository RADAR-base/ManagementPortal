package org.radarcns.management;

import io.github.jhipster.config.JHipsterConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.radarcns.management.config.ApplicationProperties;
import org.radarcns.management.config.DefaultProfileUtil;
import org.radarcns.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@ComponentScan({
        "org.radarcns.management.config",
        "org.radarcns.management.domain.support",
        "org.radarcns.management.filters",
        "org.radarcns.management.service",
        "org.radarcns.management.security",
        "org.radarcns.management.web"
})
@EnableZuulProxy
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class,
        MetricRepositoryAutoConfiguration.class})
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
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles
                .contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run "
                    + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles
                .contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
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
        log.info("\n-----------------------------------------------------\n\t"
                        + "Application '{}' is running! Access URLs:\n\t"
                        + "Local: \t\t{}://localhost:{}\n\t"
                        + "External: \t{}://{}:{}\n\t"
                        + "Profile(s): \t{}\n-----------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }
}
