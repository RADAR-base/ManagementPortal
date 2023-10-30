package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.management.security.Constants;
import org.radarbase.management.security.NotAuthorizedException;
import org.radarbase.management.service.AuthService;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.SourceDataService;
import org.radarbase.management.service.dto.SourceDataDTO;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.radarbase.auth.authorization.Permission.SOURCEDATA_CREATE;
import static org.radarbase.auth.authorization.Permission.SOURCEDATA_DELETE;
import static org.radarbase.auth.authorization.Permission.SOURCEDATA_READ;
import static org.radarbase.auth.authorization.Permission.SOURCEDATA_UPDATE;
import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_DATA;

/**
 * REST controller for managing SourceData.
 */
@RestController
@RequestMapping("/api")
public class SourceDataResource {

    private static final Logger log = LoggerFactory.getLogger(SourceDataResource.class);

    @Autowired
    private SourceDataService sourceDataService;
    @Autowired
    private AuthService authService;

    /**
     * POST  /source-data : Create a new sourceData.
     *
     * @param sourceDataDto the sourceDataDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDataDto, or
     *     with status 400 (Bad Request) if the sourceData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-data")
    @Timed
    public ResponseEntity<SourceDataDTO> createSourceData(@Valid @RequestBody SourceDataDTO
            sourceDataDto) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save SourceData : {}", sourceDataDto);
        authService.checkPermission(SOURCEDATA_CREATE);
        if (sourceDataDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(SOURCE_DATA,
                    "idexists", "A new sourceData cannot already have an ID")).build();
        }
        String name = sourceDataDto.getSourceDataName();
        if (sourceDataService.findOneBySourceDataName(name).isPresent()) {
            throw new ConflictException("SourceData already available with source-name",
                SOURCE_DATA, "error.sourceDataNameAvailable",
                    Collections.singletonMap("sourceDataName", name));
        }
        SourceDataDTO result = sourceDataService.save(sourceDataDto);
        return ResponseEntity.created(ResourceUriService.getUri(sourceDataDto))
                .headers(HeaderUtil.createEntityCreationAlert(SOURCE_DATA, name))
                .body(result);
    }

    /**
     * PUT  /source-data : Updates an existing sourceData.
     *
     * @param sourceDataDto the sourceDataDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDataDto, or
     *     with status 400 (Bad Request) if the sourceDataDto is not valid, or with status 500
     *     (Internal Server Error) if the sourceDataDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-data")
    @Timed
    public ResponseEntity<SourceDataDTO> updateSourceData(@Valid @RequestBody SourceDataDTO
            sourceDataDto) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update SourceData : {}", sourceDataDto);
        if (sourceDataDto.getId() == null) {
            return createSourceData(sourceDataDto);
        }
        authService.checkPermission(SOURCEDATA_UPDATE);
        SourceDataDTO result = sourceDataService.save(sourceDataDto);
        return ResponseEntity.ok().headers(HeaderUtil
                .createEntityUpdateAlert(SOURCE_DATA, sourceDataDto.getSourceDataName()))
                .body(result);
    }

    /**
     * GET  /source-data : get all the sourceData.
     *
     * @param pageable parameters
     * @return the ResponseEntity with status 200 (OK) and the list of sourceData in body
     */
    @GetMapping("/source-data")
    @Timed
    public ResponseEntity<List<SourceDataDTO>> getAllSourceData(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable)
            throws NotAuthorizedException {
        log.debug("REST request to get all SourceData");
        authService.checkScope(SOURCEDATA_READ);
        Page<SourceDataDTO> page = sourceDataService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/source-data");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /source-data/:sourceDataName : get the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDataDTO, or with
     *     status 404 (Not Found)
     */
    @GetMapping("/source-data/{sourceDataName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SourceDataDTO> getSourceData(@PathVariable String sourceDataName)
            throws NotAuthorizedException {
        authService.checkScope(SOURCEDATA_READ);
        return ResponseUtil.wrapOrNotFound(sourceDataService
                .findOneBySourceDataName(sourceDataName));
    }

    /**
     * DELETE  /source-data/:sourceDataName : delete the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/source-data/{sourceDataName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSourceData(@PathVariable String sourceDataName)
            throws NotAuthorizedException {
        authService.checkPermission(SOURCEDATA_DELETE);
        Optional<SourceDataDTO> sourceDataDto = sourceDataService
                .findOneBySourceDataName(sourceDataName);
        if (sourceDataDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        sourceDataService.delete(sourceDataDto.get().getId());
        return ResponseEntity.ok().headers(HeaderUtil
                .createEntityDeletionAlert(SOURCE_DATA, sourceDataName)).build();
    }

}
