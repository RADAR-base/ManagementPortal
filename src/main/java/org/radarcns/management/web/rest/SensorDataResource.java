package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.service.SensorDataService;
import org.radarcns.management.service.dto.SensorDataDTO;
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
 * REST controller for managing SensorData.
 */
@RestController
@RequestMapping("/api")
public class SensorDataResource {

    private final Logger log = LoggerFactory.getLogger(SensorDataResource.class);

    private static final String ENTITY_NAME = "sensorData";

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /sensor-data : Create a new sensorData.
     *
     * @param sensorDataDTO the sensorDataDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sensorDataDTO, or with status 400 (Bad Request) if the sensorData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sensor-data")
    @Timed
    public ResponseEntity<SensorDataDTO> createSensorData(@Valid @RequestBody SensorDataDTO sensorDataDTO) throws URISyntaxException {
        log.debug("REST request to save SensorData : {}", sensorDataDTO);
        checkPermission(getJWT(servletRequest), SENSORDATA_CREATE);
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
    public ResponseEntity<SensorDataDTO> updateSensorData(@Valid @RequestBody SensorDataDTO sensorDataDTO) throws URISyntaxException {
        log.debug("REST request to update SensorData : {}", sensorDataDTO);
        if (sensorDataDTO.getId() == null) {
            return createSensorData(sensorDataDTO);
        }
        checkPermission(getJWT(servletRequest), SENSORDATA_UPDATE);
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
        checkPermission(getJWT(servletRequest), SENSORDATA_READ);
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
        checkPermission(getJWT(servletRequest), SENSORDATA_CREATE);
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
        checkPermission(getJWT(servletRequest), SENSORDATA_DELETE);
        sensorDataService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
