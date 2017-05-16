package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.domain.SensorData;

import org.radarcns.management.repository.SensorDataRepository;
import org.radarcns.management.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing SensorData.
 */
@RestController
@RequestMapping("/api")
public class SensorDataResource {

    private final Logger log = LoggerFactory.getLogger(SensorDataResource.class);

    private static final String ENTITY_NAME = "sensorData";
        
    private final SensorDataRepository sensorDataRepository;

    public SensorDataResource(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    /**
     * POST  /sensor-data : Create a new sensorData.
     *
     * @param sensorData the sensorData to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sensorData, or with status 400 (Bad Request) if the sensorData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sensor-data")
    @Timed
    public ResponseEntity<SensorData> createSensorData(@Valid @RequestBody SensorData sensorData) throws URISyntaxException {
        log.debug("REST request to save SensorData : {}", sensorData);
        if (sensorData.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new sensorData cannot already have an ID")).body(null);
        }
        SensorData result = sensorDataRepository.save(sensorData);
        return ResponseEntity.created(new URI("/api/sensor-data/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /sensor-data : Updates an existing sensorData.
     *
     * @param sensorData the sensorData to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sensorData,
     * or with status 400 (Bad Request) if the sensorData is not valid,
     * or with status 500 (Internal Server Error) if the sensorData couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sensor-data")
    @Timed
    public ResponseEntity<SensorData> updateSensorData(@Valid @RequestBody SensorData sensorData) throws URISyntaxException {
        log.debug("REST request to update SensorData : {}", sensorData);
        if (sensorData.getId() == null) {
            return createSensorData(sensorData);
        }
        SensorData result = sensorDataRepository.save(sensorData);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sensorData.getId().toString()))
            .body(result);
    }

    /**
     * GET  /sensor-data : get all the sensorData.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sensorData in body
     */
    @GetMapping("/sensor-data")
    @Timed
    public List<SensorData> getAllSensorData() {
        log.debug("REST request to get all SensorData");
        List<SensorData> sensorData = sensorDataRepository.findAll();
        return sensorData;
    }

    /**
     * GET  /sensor-data/:id : get the "id" sensorData.
     *
     * @param id the id of the sensorData to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sensorData, or with status 404 (Not Found)
     */
    @GetMapping("/sensor-data/{id}")
    @Timed
    public ResponseEntity<SensorData> getSensorData(@PathVariable Long id) {
        log.debug("REST request to get SensorData : {}", id);
        SensorData sensorData = sensorDataRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(sensorData));
    }

    /**
     * DELETE  /sensor-data/:id : delete the "id" sensorData.
     *
     * @param id the id of the sensorData to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sensor-data/{id}")
    @Timed
    public ResponseEntity<Void> deleteSensorData(@PathVariable Long id) {
        log.debug("REST request to delete SensorData : {}", id);
        sensorDataRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
