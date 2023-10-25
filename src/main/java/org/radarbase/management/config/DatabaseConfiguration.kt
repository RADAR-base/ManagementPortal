package org.radarbase.management.config

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module
import liquibase.integration.spring.SpringLiquibase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import tech.jhipster.config.JHipsterConstants
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["org.radarbase.management.repository"],
    repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean::class
)
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
open class DatabaseConfiguration {
    @Autowired
    private val env: Environment? = null
    @Bean
    open fun liquibase(
        dataSource: DataSource?,
        liquibaseProperties: LiquibaseProperties
    ): SpringLiquibase {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start
        // asynchronously
        val liquibase = SpringLiquibase()
        liquibase.dataSource = dataSource
        liquibase.changeLog = "classpath:config/liquibase/master.xml"
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        if (env!!.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            liquibase.setShouldRun(false)
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled)
            log.debug("Configuring Liquibase")
        }
        return liquibase
    }

    @Bean
    open fun hibernate5Module(): Hibernate5Module {
        return Hibernate5Module()
    }

    companion object {
        private val log = LoggerFactory.getLogger(DatabaseConfiguration::class.java)
    }
}
