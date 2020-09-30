package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.service.RevisionService;
import org.radarcns.management.service.dto.RevisionInfoDTO;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.Date;
import java.util.List;

import static org.radarcns.auth.authorization.Permission.AUDIT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

@RestController
@RequestMapping("/api")
public class RevisionResource {

    private final Logger log = LoggerFactory.getLogger(RevisionResource.class);

    @Autowired
    private RevisionService revisionService;

    /**
     * Pageable API to get revisions.
     *
     * @param pageable the page information
     * @return the requested page of revisions
     */
    @GetMapping("/revisions")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN})
    public ResponseEntity<List<RevisionInfoDTO>> getRevisions(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable) {
        log.debug("REST request to get page of revisions");
        Page<RevisionInfoDTO> page = revisionService.getRevisions(pageable);
        return new ResponseEntity<>(page.getContent(), PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/revisions"), HttpStatus.OK);
    }

    @GetMapping(value = "/revisions", params = {"fromDate", "toDate"})
    public ResponseEntity<List<RevisionInfoDTO>> getRevisionsByDates(
            @RequestParam(value="fromDate")   @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date fromDate,
            @RequestParam(value="toDate")     @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date toDate,
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable) throws NotAuthorizedException {
        log.debug("REST request to get revisions");
        Page<RevisionInfoDTO> page = revisionService
                .findByDates(fromDate, toDate, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/revisions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Get a single revision.
     *
     * @param id the revision number
     * @return the requested revision
     */
    @GetMapping("/revisions/{id}")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN})
    public ResponseEntity<RevisionInfoDTO> getRevision(@PathVariable("id") Integer id) {
        log.debug("REST request to get single revision: {}", id.toString());
        return ResponseEntity.ok(revisionService.getRevision(id));
    }
}
