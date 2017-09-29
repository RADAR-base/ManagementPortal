package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.service.SensorDataService;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.service.dto.SensorDataDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing SensorData.
 */
@RestController
@RequestMapping("/api")
public class SensorDataResource {

    private final Logger log = LoggerFactory.getLogger(SensorDataResource.class);

    private static final String ENTITY_NAME = "sensorData";

    private final SensorDataService sensorDataService;

    public SensorDataResource(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    /**
     * POST  /sensor-data : Create a new sensorData.
     *
     * @param sensorDataDTO the sensorDataDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sensorDataDTO, or with status 400 (Bad Request) if the sensorData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sensor-data")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<SensorDataDTO> createSensorData(@Valid @RequestBody SensorDataDTO sensorDataDTO) throws URISyntaxException {
        log.debug("REST request to save SensorData : {}", sensorDataDTO);
        if (sensorDataDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new sensorData cannot already have an ID")).body(null);
        }
        SensorDataDTO result = sensorDataService.save(sensorDataDTO);
        return ResponseEntity.created(new URI("/api/sensor-data/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /sensor-data : Updates an existing sensorData.
     *
     * @param sensorDataDTO the sensorDataDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sensorDataDTO,
     * or with status 400 (Bad Request) if the sensorDataDTO is not valid,
     * or with status 500 (Internal Server Error) if the sensorDataDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sensor-data")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<SensorDataDTO> updateSensorData(@Valid @RequestBody SensorDataDTO sensorDataDTO) throws URISyntaxException {
        log.debug("REST request to update SensorData : {}", sensorDataDTO);
        if (sensorDataDTO.getId() == null) {
            return createSensorData(sensorDataDTO);
        }
        SensorDataDTO result = sensorDataService.save(sensorDataDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sensorDataDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /sensor-data : get all the sensorData.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sensorData in body
     */
    @GetMapping("/sensor-data")
    @Timed
    public List<SensorDataDTO> getAllSensorData() {
        log.debug("REST request to get all SensorData");
        return sensorDataService.findAll();
    }

    /**
     * GET  /sensor-data/:id : get the "id" sensorData.
     *
     * @param id the id of the sensorDataDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sensorDataDTO, or with status 404 (Not Found)
     */
    @GetMapping("/sensor-data/{id}")
    @Timed
    public ResponseEntity<SensorDataDTO> getSensorData(@PathVariable Long id) {
        log.debug("REST request to get SensorData : {}", id);
        SensorDataDTO sensorDataDTO = sensorDataService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(sensorDataDTO));
    }

    /**
     * DELETE  /sensor-data/:id : delete the "id" sensorData.
     *
     * @param id the id of the sensorDataDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sensor-data/{id}")
    @Timed
    public ResponseEntity<Void> deleteSensorData(@PathVariable Long id) {
        log.debug("REST request to delete SensorData : {}", id);
        sensorDataService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
