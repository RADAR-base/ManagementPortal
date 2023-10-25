package org.radarbase.management.config.audit

import org.hibernate.envers.RevisionListener
import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.radarbase.management.domain.support.AutowireHelper
import org.radarbase.management.security.Constants
import org.radarbase.management.security.SpringSecurityAuditorAware
import org.springframework.beans.factory.annotation.Autowired


class CustomRevisionListener : RevisionListener {
    @Autowired
    private val springSecurityAuditorAware: SpringSecurityAuditorAware? = null
    override fun newRevision(revisionEntity: Any) {
        AutowireHelper.autowire(this, springSecurityAuditorAware)
        val entity = revisionEntity as CustomRevisionEntity
        entity.auditor = springSecurityAuditorAware!!.currentAuditor
            .orElse(Constants.SYSTEM_ACCOUNT)
    }
}
