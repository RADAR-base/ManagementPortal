package org.radarbase.management.web.rest

import io.swagger.v3.oas.annotations.Parameter
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuditEventService
import org.radarbase.management.service.AuthService
import org.radarbase.management.web.rest.util.PaginationUtil
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.jhipster.web.util.ResponseUtil
import java.time.LocalDate

/**
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/management/audits")
class AuditResource(private val auditEventService: AuditEventService, private val authService: AuthService) {
    /**
     * GET  /audits : get a page of AuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping
    @Throws(NotAuthorizedException::class)
    fun getAll(@Parameter pageable: Pageable): ResponseEntity<List<AuditEvent?>> {
        authService.checkPermission(Permission.AUDIT_READ)
        val page = auditEventService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /audits : get a page of AuditEvents between the fromDate and toDate.
     *
     * @param fromDate the start of the time period of AuditEvents to get
     * @param toDate the end of the time period of AuditEvents to get
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping(params = ["fromDate", "toDate"])
    @Throws(NotAuthorizedException::class)
    fun getByDates(
        @RequestParam(value = "fromDate") fromDate: LocalDate,
        @RequestParam(value = "toDate") toDate: LocalDate,
        @Parameter pageable: Pageable?
    ): ResponseEntity<List<AuditEvent?>> {
        authService.checkPermission(Permission.AUDIT_READ)
        val page = auditEventService
            .findByDates(fromDate.atTime(0, 0), toDate.atTime(23, 59), pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /audits/:id : get an AuditEvent by id.
     *
     * @param id the id of the entity to get
     * @return the ResponseEntity with status 200 (OK) and the AuditEvent in body, or status
     * 404 (Not Found)
     */
    @GetMapping("/{id:.+}")
    @Throws(NotAuthorizedException::class)
    operator fun get(@PathVariable id: Long): ResponseEntity<AuditEvent?> {
        authService.checkPermission(Permission.AUDIT_READ)
        return ResponseUtil.wrapOrNotFound(auditEventService.find(id))
    }
}
