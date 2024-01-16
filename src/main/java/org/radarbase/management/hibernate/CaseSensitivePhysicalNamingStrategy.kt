package org.radarbase.management.hibernate

import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy

class CaseSensitivePhysicalNamingStrategy : SpringPhysicalNamingStrategy() {
    override fun isCaseInsensitive(jdbcEnvironment: JdbcEnvironment): Boolean {
        return false
    }
}
