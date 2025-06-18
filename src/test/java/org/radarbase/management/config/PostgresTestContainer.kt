package org.radarbase.management.config

import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestContainer {
    val container: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .apply { start() }
    }
}
