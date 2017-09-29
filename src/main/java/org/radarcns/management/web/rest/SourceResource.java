package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.service.SourceService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private static final String ENTITY_NAME = "source";

    @Autowired
    private  SourceService sourceService;

    @Autowired
    private  SourceRepository sourceRepository;

//    public SourceResource(SourceService sourceService) {
//        this.sourceService = sourceService;
//    }

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
        } else if (sourceDTO.getSourceId() != null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceIdExists", "A new source cannot already have a Source ID"))
                .body(null);
        } else if (sourceRepository.findOneBySourceName(sourceDTO.getSourceName()).isPresent()) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceNameExists", "Source name already in use"))
                .body(null);
        } else {
            SourceDTO result = sourceService.save(sourceDTO);
            return ResponseEntity.created(new URI("/api/sources/" + result.getId()))
                .headers(
                    HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
        }
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
        if (sourceRepository.findOne(sourceDTO.getId()).isAssigned()) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceIsAssigned", "Cannot delete an assigned source"))
                .body(null);
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
    public ResponseEntity getAllSources(
        @RequestParam(value = "projectId" , required = false) Long projectId) {

        if( projectId!=null) {
            log.debug("REST request to get all Sources by project id {}" , projectId);
            return ResponseUtil.wrapOrNotFound(Optional.of(sourceService.findAllByProjectId(projectId)));
        }
        log.debug("REST request to get all Sources");
        return ResponseUtil.wrapOrNotFound(Optional.of(sourceService.findAll()));
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
        return sourceService.findAllUnassignedSourcesMinimalDTO();
    }

    /**
     * GET  /sources/project/{projectId} : get all the sources by project
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources/project/{projectId}")
    @Timed
    public List<MinimalSourceDetailsDTO> getAllSourcesForProject(@PathVariable Long projectId,
        @RequestParam(value = "assigned", required = false) Boolean assigned) {
        log.debug("REST request to get all Sources");
        if(assigned !=null) {
            return sourceService.findAllByProjectAndAssigned(projectId, assigned);
        }
        return sourceService.findAllMinimalSourceDetailsByProject(projectId);
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
        if (sourceRepository.findOne(id).isAssigned()) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceIsAssigned", "Cannot delete an assigned source"))
                .body(null);
        }
        sourceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
