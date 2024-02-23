package org.radarbase.management.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.Nonnull

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {
    @Autowired
    private val authentication: Optional<Authentication>? = null
    @Nonnull
    override fun getCurrentAuditor(): Optional<String> {
        return authentication!!.map { obj: Authentication -> obj.name }
            .filter { n: String -> n.isNotEmpty() }
    }
}
