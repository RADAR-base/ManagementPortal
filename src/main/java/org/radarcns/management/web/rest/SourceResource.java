package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.ProjectService;
import org.radarcns.management.service.SourceService;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.radarcns.auth.authorization.Permission.SOURCE_CREATE;
import static org.radarcns.auth.authorization.Permission.SOURCE_DELETE;
import static org.radarcns.auth.authorization.Permission.SOURCE_READ;
import static org.radarcns.auth.authorization.Permission.SOURCE_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

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
    private SourceRepository sourceRepository;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private ProjectService projectService;

    /**
     * POST  /sources : Create a new source.
     *
     * @param sourceDTO the sourceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDTO, or with status 400 (Bad Request) if the source has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody SourceDTO sourceDTO) throws URISyntaxException {
        log.debug("REST request to save Source : {}", sourceDTO);
        checkPermission(getJWT(servletRequest), SOURCE_CREATE);
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
        } else if (sourceDTO.getAssigned() == null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceAssignedRequired",
                    "A new source must have the 'assigned' field specified")).body(null);
        } else {
            SourceDTO result = sourceService.save(sourceDTO);
            return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "sources",
                    result.getSourceName())))
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getSourceName()))
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
        checkPermission(getJWT(servletRequest), SOURCE_UPDATE);
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
    public ResponseEntity<List<SourceDTO>> getAllSources() {
        log.debug("REST request to get all Sources");
        checkPermission(getJWT(servletRequest), SOURCE_READ);
        return ResponseUtil.wrapOrNotFound(Optional.of(sourceService.findAll()));
    }

    /**
     * GET  /sources/:sourceName : get the source with this sourceName
     *
     * @param sourceName the name of the sourceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/sources/{sourceName}")
    @Timed
    public ResponseEntity<SourceDTO> getSource(@PathVariable String sourceName) {
        log.debug("REST request to get Source : {}", sourceName);
        checkPermission(getJWT(servletRequest), SOURCE_READ);
        return ResponseUtil.wrapOrNotFound(sourceService.findOneByName(sourceName));
    }

    /**
     * DELETE  /sources/:sourceName : delete the "id" source.
     *
     * @param sourceName the id of the sourceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sources/{sourceName}")
    @Timed
    public ResponseEntity<Void> deleteSource(@PathVariable String sourceName) {
        log.debug("REST request to delete Source : {}", sourceName);
        checkPermission(getJWT(servletRequest), SOURCE_DELETE);
        Optional<SourceDTO> sourceDTO = sourceService.findOneByName(sourceName);
        if (!sourceDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (sourceDTO.get().getAssigned()) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceIsAssigned", "Cannot delete an assigned source"))
                .body(null);
        }
        sourceService.delete(sourceDTO.get().getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, sourceName))
            .build();
    }

}
