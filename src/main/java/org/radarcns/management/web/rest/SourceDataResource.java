package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.service.SourceDataService;
import org.radarcns.management.service.dto.SourceDataDTO;
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

import static org.radarcns.auth.authorization.Permission.SENSORDATA_CREATE;
import static org.radarcns.auth.authorization.Permission.SENSORDATA_DELETE;
import static org.radarcns.auth.authorization.Permission.SENSORDATA_READ;
import static org.radarcns.auth.authorization.Permission.SENSORDATA_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

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
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDataDTO, or with status 400 (Bad Request) if the sourceData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-data")
    @Timed
    public ResponseEntity<SourceDataDTO> createSourceData(@Valid @RequestBody SourceDataDTO sourceDataDTO) throws URISyntaxException {
        log.debug("REST request to save SourceData : {}", sourceDataDTO);
        checkPermission(getJWT(servletRequest), SENSORDATA_CREATE);
        if (sourceDataDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new sourceData cannot already have an ID")).body(null);
        }
        SourceDataDTO result = sourceDataService.save(sourceDataDTO);
        return ResponseEntity.created(new URI("/api/source-data/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
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
    public ResponseEntity<SourceDataDTO> updateSourceData(@Valid @RequestBody SourceDataDTO sourceDataDTO) throws URISyntaxException {
        log.debug("REST request to update SourceData : {}", sourceDataDTO);
        if (sourceDataDTO.getId() == null) {
            return createSourceData(sourceDataDTO);
        }
        checkPermission(getJWT(servletRequest), SENSORDATA_UPDATE);
        SourceDataDTO result = sourceDataService.save(sourceDataDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sourceDataDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /source-data : get all the sourceData.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sourceData in body
     */
    @GetMapping("/source-data")
    @Timed
    public List<SourceDataDTO> getAllSourceData() {
        log.debug("REST request to get all SourceData");
        checkPermission(getJWT(servletRequest), SENSORDATA_READ);
        return sourceDataService.findAll();
    }

    /**
     * GET  /source-data/:sourceDataName : get the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDataDTO, or with status 404 (Not Found)
     */
    @GetMapping("/source-data/{sourceDataName}")
    @Timed
    public ResponseEntity<SourceDataDTO> getSourceData(@PathVariable String sourceDataName) {
        checkPermission(getJWT(servletRequest), SENSORDATA_READ);
        return ResponseUtil.wrapOrNotFound(sourceDataService.findOneBySourceDataName(sourceDataName));
    }

    /**
     * DELETE  /source-data/:sourceDataName : delete the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/source-data/{sourceDataName}")
    @Timed
    public ResponseEntity<Void> deleteSourceData(@PathVariable String sourceDataName) {
        checkPermission(getJWT(servletRequest), SENSORDATA_DELETE);
        Optional<SourceDataDTO> sourceDataDTO = sourceDataService.findOneBySourceDataName(sourceDataName);
        if (!sourceDataDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        sourceDataService.delete(sourceDataDTO.get().getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, sourceDataName)).build();
    }

}
