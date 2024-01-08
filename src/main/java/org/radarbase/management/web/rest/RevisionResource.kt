package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.service.RevisionService;
import org.radarbase.management.service.dto.RevisionInfoDTO;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RevisionResource {

    private static final Logger log = LoggerFactory.getLogger(RevisionResource.class);

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
    @Secured({RoleAuthority.SYS_ADMIN_AUTHORITY})
    public ResponseEntity<List<RevisionInfoDTO>> getRevisions(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable) {
        log.debug("REST request to get page of revisions");
        Page<RevisionInfoDTO> page = revisionService.getRevisions(pageable);
        return new ResponseEntity<>(page.getContent(), PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/revisions"), HttpStatus.OK);
    }

    /**
     * Get a single revision.
     *
     * @param id the revision number
     * @return the requested revision
     */
    @GetMapping("/revisions/{id}")
    @Timed
    @Secured({RoleAuthority.SYS_ADMIN_AUTHORITY})
    public ResponseEntity<RevisionInfoDTO> getRevision(@PathVariable("id") Integer id) {
        log.debug("REST request to get single revision: {}", id.toString());
        return ResponseEntity.ok(revisionService.getRevision(id));
    }
}
