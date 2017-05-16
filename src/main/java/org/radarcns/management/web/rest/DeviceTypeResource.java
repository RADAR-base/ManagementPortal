package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.domain.DeviceType;

import org.radarcns.management.repository.DeviceTypeRepository;
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
 * REST controller for managing DeviceType.
 */
@RestController
@RequestMapping("/api")
public class DeviceTypeResource {

    private final Logger log = LoggerFactory.getLogger(DeviceTypeResource.class);

    private static final String ENTITY_NAME = "deviceType";
        
    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceTypeResource(DeviceTypeRepository deviceTypeRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
    }

    /**
     * POST  /device-types : Create a new deviceType.
     *
     * @param deviceType the deviceType to create
     * @return the ResponseEntity with status 201 (Created) and with body the new deviceType, or with status 400 (Bad Request) if the deviceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/device-types")
    @Timed
    public ResponseEntity<DeviceType> createDeviceType(@Valid @RequestBody DeviceType deviceType) throws URISyntaxException {
        log.debug("REST request to save DeviceType : {}", deviceType);
        if (deviceType.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new deviceType cannot already have an ID")).body(null);
        }
        DeviceType result = deviceTypeRepository.save(deviceType);
        return ResponseEntity.created(new URI("/api/device-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /device-types : Updates an existing deviceType.
     *
     * @param deviceType the deviceType to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated deviceType,
     * or with status 400 (Bad Request) if the deviceType is not valid,
     * or with status 500 (Internal Server Error) if the deviceType couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/device-types")
    @Timed
    public ResponseEntity<DeviceType> updateDeviceType(@Valid @RequestBody DeviceType deviceType) throws URISyntaxException {
        log.debug("REST request to update DeviceType : {}", deviceType);
        if (deviceType.getId() == null) {
            return createDeviceType(deviceType);
        }
        DeviceType result = deviceTypeRepository.save(deviceType);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, deviceType.getId().toString()))
            .body(result);
    }

    /**
     * GET  /device-types : get all the deviceTypes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of deviceTypes in body
     */
    @GetMapping("/device-types")
    @Timed
    public List<DeviceType> getAllDeviceTypes() {
        log.debug("REST request to get all DeviceTypes");
        List<DeviceType> deviceTypes = deviceTypeRepository.findAllWithEagerRelationships();
        return deviceTypes;
    }

    /**
     * GET  /device-types/:id : get the "id" deviceType.
     *
     * @param id the id of the deviceType to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the deviceType, or with status 404 (Not Found)
     */
    @GetMapping("/device-types/{id}")
    @Timed
    public ResponseEntity<DeviceType> getDeviceType(@PathVariable Long id) {
        log.debug("REST request to get DeviceType : {}", id);
        DeviceType deviceType = deviceTypeRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(deviceType));
    }

    /**
     * DELETE  /device-types/:id : delete the "id" deviceType.
     *
     * @param id the id of the deviceType to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/device-types/{id}")
    @Timed
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long id) {
        log.debug("REST request to delete DeviceType : {}", id);
        deviceTypeRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
