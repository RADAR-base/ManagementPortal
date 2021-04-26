package org.radarbase.management.config.audit;

import org.hibernate.envers.RevisionListener;
import org.radarbase.auth.config.Constants;
import org.radarbase.management.domain.audit.CustomRevisionEntity;
import org.radarbase.management.domain.support.AutowireHelper;
import org.radarbase.management.security.SpringSecurityAuditorAware;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomRevisionListener implements RevisionListener {

    @Autowired
    private SpringSecurityAuditorAware springSecurityAuditorAware;

    @Override
    public void newRevision(Object revisionEntity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware);
        CustomRevisionEntity entity = (CustomRevisionEntity) revisionEntity;
        entity.setAuditor(springSecurityAuditorAware.getCurrentAuditor()
                .orElse(Constants.SYSTEM_ACCOUNT));
    }
}
