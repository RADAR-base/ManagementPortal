package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.SourceService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.service.dto.SourceDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Source.
 */
@RestController
@RequestMapping("/api")
public class SourceResource {

    private final Logger log = LoggerFactory.getLogger(SourceResource.class);

    private static final String ENTITY_NAME = "soruce";

    private final SourceService sourceService;

    public SourceResource(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    /**
     * POST  /sources : Create a new source.
     *
     * @param sourceDTO the sourceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDTO, or with status 400 (Bad Request) if the source has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sources")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody SourceDTO sourceDTO) throws URISyntaxException {
        log.debug("REST request to save Source : {}", sourceDTO);
        if (sourceDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new source cannot already have an ID")).body(null);
        }
        SourceDTO result = sourceService.save(sourceDTO);
        return ResponseEntity.created(new URI("/api/sources/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /sources : Updates an existing source.
     *
     * @param sourceDTO the sourceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDTO,
     * or with status 400 (Bad Request) if the sourceDTO is not valid,
     * or with status 500 (Internal Server Error) if the sourceDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> updateSource(@Valid @RequestBody SourceDTO sourceDTO) throws URISyntaxException {
        log.debug("REST request to update Source : {}", sourceDTO);
        if (sourceDTO.getId() == null) {
            return createSource(sourceDTO);
        }
        SourceDTO result = sourceService.save(sourceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sourceDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /sources : get all the sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources")
    @Timed
    public List<SourceDTO> getAllSources() {
        log.debug("REST request to get all Sources");
        return sourceService.findAll();
    }

    /**
     * GET  /sources : get all the unassigned sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources/unassigned")
    @Timed
    public List<MinimalSourceDetailsDTO> getAllUnassignedSources() {
        log.debug("REST request to get all Sources");
        return sourceService.findAllUnassignedSources();
    }

    /**
     * GET  /sources : get all the sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources/unassigned/subject/{id}")
    @Timed
    public List<MinimalSourceDetailsDTO> getAllUnassignedSourcesAndOfSubject(@PathVariable Long id) {
        log.debug("REST request to get all Sources");
        return sourceService.findAllUnassignedSourcesAndOfSubject(id);
    }


    /**
     * GET  /sources/:id : get the "id" source.
     *
     * @param id the id of the sourceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/sources/{id}")
    @Timed
    public ResponseEntity<SourceDTO> getSource(@PathVariable Long id) {
        log.debug("REST request to get Source : {}", id);
        SourceDTO sourceDTO = sourceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(sourceDTO));
    }

    /**
     * DELETE  /sources/:id : delete the "id" source.
     *
     * @param id the id of the sourceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sources/{id}")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        log.debug("REST request to delete Source : {}", id);
        sourceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
