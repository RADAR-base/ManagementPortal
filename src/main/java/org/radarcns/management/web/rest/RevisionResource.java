package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import org.radarcns.management.service.RevisionService;
import org.radarcns.management.service.dto.RevisionInfoDTO;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RevisionResource {

    private final Logger log = LoggerFactory.getLogger(RevisionResource.class);

    private static final String ENTITY_NAME = "REVISION";

    @Autowired
    private RevisionService revisionService;

    @GetMapping("/revisions")
    @Timed
    public ResponseEntity<List<RevisionInfoDTO>> getRevisions(@ApiParam Pageable pageable) {
        Page<RevisionInfoDTO> page = revisionService.getRevisions(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/revisions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @GetMapping("/revisions/{id}")
    @Timed
    public ResponseEntity<RevisionInfoDTO> getRevision(@PathVariable("id") Long id) {
        return ResponseEntity.ok(revisionService.getRevision(id));
    }
}
