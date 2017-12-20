package org.radarcns.management.web.rest;

import static org.radarcns.auth.authorization.Permission.SOURCEDATA_CREATE;
import static org.radarcns.auth.authorization.Permission.SOURCEDATA_DELETE;
import static org.radarcns.auth.authorization.Permission.SOURCEDATA_READ;
import static org.radarcns.auth.authorization.Permission.SOURCEDATA_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.service.SourceDataService;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.Collections;
/**
 * REST controller for managing SourceData.
 */
@RestController
@RequestMapping("/api")
public class SourceDataResource {

    private final Logger log = LoggerFactory.getLogger(SourceDataResource.class);

    private static final String ENTITY_NAME = "sourceData";

    @Autowired
    private SourceDataService sourceDataService;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /source-data : Create a new sourceData.
     *
     * @param sourceDataDTO the sourceDataDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDataDTO, or
     * with status 400 (Bad Request) if the sourceData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-data")
    @Timed
    public ResponseEntity<SourceDataDTO> createSourceData(@Valid @RequestBody SourceDataDTO
            sourceDataDTO) throws URISyntaxException {
        log.debug("REST request to save SourceData : {}", sourceDataDTO);
        checkPermission(getJWT(servletRequest), SOURCEDATA_CREATE);
        if (sourceDataDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "A new sourceData cannot already have an ID")).build();
        }
        String name = sourceDataDTO.getSourceDataName();
        if(sourceDataService.findOneBySourceDataName(name).isPresent()) {
            throw new CustomConflictException("error.sourceDataNameAvailable",
                    Collections.singletonMap("sourceDataName", name),
                    new URI(HeaderUtil.buildPath("api", "source-data", name)));
        }
        SourceDataDTO result = sourceDataService.save(sourceDataDTO);
        return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "source-data", name)))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, name))
                .body(result);
    }

    /**
     * PUT  /source-data : Updates an existing sourceData.
     *
     * @param sourceDataDTO the sourceDataDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDataDTO,
     * or with status 400 (Bad Request) if the sourceDataDTO is not valid,
     * or with status 500 (Internal Server Error) if the sourceDataDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-data")
    @Timed
    public ResponseEntity<SourceDataDTO> updateSourceData(@Valid @RequestBody SourceDataDTO
            sourceDataDTO) throws URISyntaxException {
        log.debug("REST request to update SourceData : {}", sourceDataDTO);
        if (sourceDataDTO.getId() == null) {
            return createSourceData(sourceDataDTO);
        }
        checkPermission(getJWT(servletRequest), SOURCEDATA_UPDATE);
        SourceDataDTO result = sourceDataService.save(sourceDataDTO);
        return ResponseEntity.ok().headers(HeaderUtil
                .createEntityUpdateAlert(ENTITY_NAME, sourceDataDTO.getSourceDataName()))
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
    public ResponseEntity<List<SourceDataDTO>> getAllSourceData(@ApiParam Pageable pageable) {
        log.debug("REST request to get all SourceData");
        checkPermission(getJWT(servletRequest), SOURCEDATA_READ);
        Page<SourceDataDTO> page = sourceDataService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/source-data");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /source-data/:sourceDataName : get the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDataDTO, or with
     * status 404 (Not Found)
     */
    @GetMapping("/source-data/{sourceDataName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SourceDataDTO> getSourceData(@PathVariable String sourceDataName) {
        checkPermission(getJWT(servletRequest), SOURCEDATA_READ);
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
    public ResponseEntity<Void> deleteSourceData(@PathVariable String sourceDataName) {
        checkPermission(getJWT(servletRequest), SOURCEDATA_DELETE);
        Optional<SourceDataDTO> sourceDataDTO = sourceDataService
                .findOneBySourceDataName(sourceDataName);
        if (!sourceDataDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        sourceDataService.delete(sourceDataDTO.get().getId());
        return ResponseEntity.ok().headers(HeaderUtil
                .createEntityDeletionAlert(ENTITY_NAME, sourceDataName)).build();
    }

}
