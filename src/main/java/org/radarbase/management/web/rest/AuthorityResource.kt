package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.dto.AuthorityDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Stream

/**
 * REST controller for managing Authority.
 */
@RestController
@RequestMapping("/api")
class AuthorityResource {
    @Autowired
    private val authService: AuthService? = null

    @get:Throws(NotAuthorizedException::class)
    @get:Timed
    @get:GetMapping("/authorities")
    val allAuthorities: List<AuthorityDTO>
        /**
         * GET  /authorities : get all the authorities.
         *
         * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
         */
        get() {
            log.debug("REST request to get all Authorities")
            authService!!.checkScope(Permission.AUTHORITY_READ)
            return ALL_AUTHORITIES
        }

    companion object {
        private val log = LoggerFactory.getLogger(AuthorityResource::class.java)
        private val ALL_AUTHORITIES =
            Stream
                .of(
                    RoleAuthority.SYS_ADMIN,
                    RoleAuthority.ORGANIZATION_ADMIN,
                    RoleAuthority.PROJECT_ADMIN,
                    RoleAuthority.PROJECT_OWNER,
                    RoleAuthority.PROJECT_AFFILIATE,
                    RoleAuthority.PROJECT_ANALYST,
                ).map { role: RoleAuthority? ->
                    AuthorityDTO(
                        role!!,
                    )
                }.toList()
    }
}
