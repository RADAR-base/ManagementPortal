package org.radarbase.management.config

import org.junit.jupiter.api.TestInstance
import org.radarbase.management.ManagementPortalTestApp
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BasePostgresIntegrationTest {

    companion object {

        private val container = PostgresTestContainer.container


        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { container.jdbcUrl }
            registry.add("spring.datasource.username") { container.username }
            registry.add("spring.datasource.password") { container.password }
            registry.add("spring.datasource.driver-class-name") { container.driverClassName }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
        }
    }
}
