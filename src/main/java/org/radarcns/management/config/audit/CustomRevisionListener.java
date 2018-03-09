package org.radarcns.management.config.audit;

import org.hibernate.envers.RevisionListener;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.domain.support.AutowireHelper;
import org.radarcns.management.security.SpringSecurityAuditorAware;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomRevisionListener implements RevisionListener {

    @Autowired
    private SpringSecurityAuditorAware springSecurityAuditorAware;

    @Override
    public void newRevision(Object revisionEntity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware);
        CustomRevisionEntity entity = (CustomRevisionEntity) revisionEntity;
        entity.setAuditor(springSecurityAuditorAware.getCurrentAuditor());
    }
}