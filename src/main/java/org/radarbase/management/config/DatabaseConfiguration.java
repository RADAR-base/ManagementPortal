package org.radarbase.management.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tech.jhipster.config.JHipsterConstants;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = {"org.radarbase.management.repository","uk.ac.herc.common.security.mf"},
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement


public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource,
            LiquibaseProperties liquibaseProperties) {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start
        // asynchronously
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:config/liquibase/master.xml");
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }
        return liquibase;
    }

    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }
}
