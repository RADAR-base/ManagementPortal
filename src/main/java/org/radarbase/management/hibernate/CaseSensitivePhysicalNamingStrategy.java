package org.radarbase.management.hibernate;

import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class CaseSensitivePhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    @Override
    protected boolean isCaseInsensitive(JdbcEnvironment jdbcEnvironment) {
        return true;
    }
}
