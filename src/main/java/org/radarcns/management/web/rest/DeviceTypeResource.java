package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.DeviceTypeService;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.service.dto.DeviceTypeDTO;
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
     * @return the ResponseEntity with status 200 (OK) and the list of deviceTypes in body
     */
    @GetMapping("/device-types")
    @Timed
    public List<DeviceTypeDTO> getAllDeviceTypes() {
        log.debug("REST request to get all DeviceTypes");
        return deviceTypeService.findAll();
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
