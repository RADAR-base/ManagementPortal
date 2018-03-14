package org.radarcns.management.config.audit;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class AuditReaderConfiguration {

    @Autowired
    private EntityManager entityManager;

    @Bean
    public AuditReader auditReader() {
        return AuditReaderFactory.get(entityManager);
    }
}
