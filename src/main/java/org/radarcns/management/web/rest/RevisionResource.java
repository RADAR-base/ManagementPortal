package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import org.radarcns.management.service.RevisionService;
import org.radarcns.management.service.dto.RevisionInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RevisionResource {

    private final Logger log = LoggerFactory.getLogger(RevisionResource.class);

    private static final String ENTITY_NAME = "REVISION";

    @Autowired
    private RevisionService revisionService;

    @GetMapping("/revisions")
    @Timed
    public Page<RevisionInfoDTO> getRevisions(@ApiParam Pageable pageable) {
        return revisionService.getRevisions(pageable);
    }


    @GetMapping("/revisions/{id}")
    @Timed
    public RevisionInfoDTO getRevision(@PathVariable("id") Long id) {
        return revisionService.getRevision(id);
    }
}
