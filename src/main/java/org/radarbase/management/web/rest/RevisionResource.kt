package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.service.RevisionService
import org.radarbase.management.service.dto.RevisionInfoDTO
import org.radarbase.management.web.rest.util.PaginationUtil.generatePaginationHttpHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RevisionResource {
    @Autowired
    private val revisionService: RevisionService? = null

    /**
     * Pageable API to get revisions.
     *
     * @param pageable the page information
     * @return the requested page of revisions
     */
    @GetMapping("/revisions")
    @Timed
    @Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    fun getRevisions(
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable?,
    ): ResponseEntity<List<RevisionInfoDTO>> {
        log.debug("REST request to get page of revisions")
        val page = revisionService!!.getRevisions(pageable!!)
        return ResponseEntity(page.content, generatePaginationHttpHeaders(page, "/api/revisions"), HttpStatus.OK)
    }

    /**
     * Get a single revision.
     *
     * @param id the revision number
     * @return the requested revision
     */
    @GetMapping("/revisions/{id}")
    @Timed
    @Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    fun getRevision(
        @PathVariable("id") id: Int,
    ): ResponseEntity<RevisionInfoDTO> {
        log.debug("REST request to get single revision: {}", id.toString())
        return ResponseEntity.ok(revisionService!!.getRevision(id))
    }

    companion object {
        private val log = LoggerFactory.getLogger(RevisionResource::class.java)
    }
}
