package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.repository.DeviceTypeRepository;
import org.radarcns.management.service.DeviceTypeService;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.web.rest.errors.CustomConflictException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.radarcns.auth.authorization.Permission.DEVICETYPE_CREATE;
import static org.radarcns.auth.authorization.Permission.DEVICETYPE_DELETE;
import static org.radarcns.auth.authorization.Permission.DEVICETYPE_READ;
import static org.radarcns.auth.authorization.Permission.DEVICETYPE_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing DeviceType.
 */
@RestController
@RequestMapping("/api")
public class DeviceTypeResource {

    private final Logger log = LoggerFactory.getLogger(DeviceTypeResource.class);

    private static final String ENTITY_NAME = "deviceType";

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /device-types : Create a new deviceType.
     *
     * @param deviceTypeDTO the deviceTypeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new deviceTypeDTO, or with status 400 (Bad Request) if the deviceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/device-types")
    @Timed
    public ResponseEntity<DeviceTypeDTO> createDeviceType(@Valid @RequestBody DeviceTypeDTO deviceTypeDTO) throws URISyntaxException {
        log.debug("REST request to save DeviceType : {}", deviceTypeDTO);
        checkPermission(getJWT(servletRequest), DEVICETYPE_CREATE);
        if (deviceTypeDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new deviceType cannot already have an ID")).body(null);
        }
        Optional<DeviceType> existing = deviceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(deviceTypeDTO.getDeviceProducer(), deviceTypeDTO.getDeviceModel(), deviceTypeDTO.getCatalogVersion());
        if (existing.isPresent()) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "A DeviceType with the specified producer and model "
                + "already exists. This combination needs to be unique.");
            errorParams.put("producer", deviceTypeDTO.getDeviceProducer());
            errorParams.put("model", deviceTypeDTO.getDeviceModel());
            throw new CustomConflictException("deviceTypeAvailable", errorParams);
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
        checkPermission(getJWT(servletRequest), DEVICETYPE_UPDATE);
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
    public ResponseEntity<List<DeviceTypeDTO>> getAllDeviceTypes() {
        checkPermission(getJWT(servletRequest), DEVICETYPE_READ);
        return ResponseEntity.ok(deviceTypeService.findAll());
    }

    /**
     * Find the list of DeviceTypes made by the given producer
     * @param producer The producer
     * @return A list of objects matching the producer
     */
    @GetMapping("/device-types/{producer}")
    @Timed
    public ResponseEntity<List<DeviceTypeDTO>> getDeviceTypes(@PathVariable String producer) {
        checkPermission(getJWT(servletRequest), DEVICETYPE_READ);
        return ResponseEntity.ok(deviceTypeService.findByProducer(producer));
    }

    /**
     * Find the list of DeviceTypes of the given producer and model. Can be multiple since
     * multiple version of a single model can be made.
     *
     * @param producer The producer
     * @param model The model
     * @return A list of objects matching the producer and model
     */
    @GetMapping("/device-types/{producer}/{model}")
    @Timed
    public ResponseEntity<List<DeviceTypeDTO>> getDeviceTypes(@PathVariable String producer,
            @PathVariable String model) {
        checkPermission(getJWT(servletRequest), DEVICETYPE_READ);
        return ResponseEntity.ok(deviceTypeService.findByProducerAndModel(producer, model));
    }

    /**
     * Find the DeviceType of the given producer, model and version
     *
     * @param producer The producer
     * @param model The model
     * @param version The version
     * @return A single DeviceType object matching the producer, model and version
     */
    @GetMapping("/device-types/{producer}/{model}/{version}")
    @Timed
    public ResponseEntity<DeviceTypeDTO> getDeviceTypes(@PathVariable String producer,
        @PathVariable String model, @PathVariable String version) {
        checkPermission(getJWT(servletRequest), DEVICETYPE_READ);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(
            deviceTypeService.findByProducerAndModelAndVersion(producer, model, version)));
    }

    /**
     * DELETE  /device-types/:producer/:model/:version : delete the deviceType with the specified
     * producer, model and version
     *
     * @param producer The producer
     * @param model The model
     * @param version The version
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/device-types/{producer}/{model}/{version}")
    @Timed
    public ResponseEntity<Void> deleteDeviceType(@PathVariable String producer,
        @PathVariable String model, @PathVariable String version) {
        checkPermission(getJWT(servletRequest), DEVICETYPE_DELETE);
        DeviceTypeDTO deviceTypeDTO = deviceTypeService
            .findByProducerAndModelAndVersion(producer, model, version);
        if (Objects.isNull(deviceTypeDTO)) {
            return ResponseEntity.notFound().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                "notfound", String.join(" ", producer, model, version))).build();
        }
        deviceTypeService.delete(deviceTypeDTO.getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
            String.join(" ", producer, model, version))).build();
    }

}
