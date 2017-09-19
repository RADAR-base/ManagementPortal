package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.DeviceTypeService;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing DeviceType.
 */
@RestController
@RequestMapping("/api")
public class DeviceTypeResource {

    private final Logger log = LoggerFactory.getLogger(DeviceTypeResource.class);

    private static final String ENTITY_NAME = "deviceType";

    private final DeviceTypeService deviceTypeService;

    public DeviceTypeResource(DeviceTypeService deviceTypeService) {
        this.deviceTypeService = deviceTypeService;
    }

    /**
     * POST  /device-types : Create a new deviceType.
     *
     * @param deviceTypeDTO the deviceTypeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new deviceTypeDTO, or with status 400 (Bad Request) if the deviceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/device-types")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<DeviceTypeDTO> createDeviceType(@Valid @RequestBody DeviceTypeDTO deviceTypeDTO) throws URISyntaxException {
        log.debug("REST request to save DeviceType : {}", deviceTypeDTO);
        if (deviceTypeDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new deviceType cannot already have an ID")).body(null);
        }
        DeviceTypeDTO existing = deviceTypeService.findByProducerAndModel(
            deviceTypeDTO.getDeviceProducer(), deviceTypeDTO.getDeviceModel());
        if (existing != null) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "A DeviceType with the specified producer and model "
                + "already exists. This combination needs to be unique.");
            errorParams.put("producer", deviceTypeDTO.getDeviceProducer());
            errorParams.put("model", deviceTypeDTO.getDeviceModel());
            throw new CustomConflictException("Conflict", errorParams);
        }
        DeviceTypeDTO result = deviceTypeService.save(deviceTypeDTO);
        return ResponseEntity.created(new URI("/api/device-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /device-types : Updates an existing deviceType.
     *
     * @param deviceTypeDTO the deviceTypeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated deviceTypeDTO,
     * or with status 400 (Bad Request) if the deviceTypeDTO is not valid,
     * or with status 500 (Internal Server Error) if the deviceTypeDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/device-types")
    @Timed
    public ResponseEntity<DeviceTypeDTO> updateDeviceType(@Valid @RequestBody DeviceTypeDTO deviceTypeDTO) throws URISyntaxException {
        log.debug("REST request to update DeviceType : {}", deviceTypeDTO);
        // we will also allow updating with no id, but with producer and model specified
        if (deviceTypeDTO.getDeviceProducer() != null && deviceTypeDTO.getDeviceModel() != null) {
            DeviceTypeDTO existing = deviceTypeService.findByProducerAndModel(
                deviceTypeDTO.getDeviceProducer(), deviceTypeDTO.getDeviceModel());
            if (existing != null) {
                deviceTypeDTO.setId(existing.getId());
            }
        }
        if (deviceTypeDTO.getId() == null) {
            return createDeviceType(deviceTypeDTO);
        }
        DeviceTypeDTO result = deviceTypeService.save(deviceTypeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, deviceTypeDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /device-types : get all the deviceTypes.
     *
     * @param producer If this parameter is supplied, return only the DeviceTypes for which the
     *                 deviceProducer matches this value
     * @param model If this parameter is supplied, return only the DeviceTypes for which the
     *              deviceModel matches this value
     * @return the ResponseEntity with status 200 (OK) and the list of deviceTypes in body
     */
    @GetMapping("/device-types")
    @Timed
    public ResponseEntity<List<DeviceTypeDTO>> getAllDeviceTypes(@RequestParam(required = false) String producer,
            @RequestParam(required = false) String model) {
        List<DeviceTypeDTO> result;
        if (producer != null && model != null) {
            log.debug("REST request to get DeviceTypes for producer {} and model {}", producer, model);
            DeviceTypeDTO deviceTypeDTO = deviceTypeService.findByProducerAndModel(producer, model);
            if (deviceTypeDTO == null) {
                result = Collections.emptyList();
            }
            else {
                result = Collections.singletonList(deviceTypeDTO);
            }
        }
        else if (producer != null) {
            log.debug("REST request to get DeviceTypes for producer {}", producer);
            result = deviceTypeService.findByProducer(producer);
        }
        else if (model != null) {
            log.debug("REST request to get DeviceTypes for model {}", model);
            result = deviceTypeService.findByModel(model);
        }
        else {
            log.debug("REST request to get all DeviceTypes");
            result = deviceTypeService.findAll();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /device-types/:id : get the "id" deviceType.
     *
     * @param id the id of the deviceTypeDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the deviceTypeDTO, or with status 404 (Not Found)
     */
    @GetMapping("/device-types/{id}")
    @Timed
    public ResponseEntity<DeviceTypeDTO> getDeviceType(@PathVariable Long id) {
        log.debug("REST request to get DeviceType : {}", id);
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(deviceTypeDTO));
    }

    /**
     * DELETE  /device-types/:id : delete the "id" deviceType.
     *
     * @param id the id of the deviceTypeDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/device-types/{id}")
    @Timed
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long id) {
        log.debug("REST request to delete DeviceType : {}", id);
        deviceTypeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
