package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.SourceTypeRepository;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.radarcns.auth.authorization.Permission.SOURCETYPE_CREATE;
import static org.radarcns.auth.authorization.Permission.SOURCETYPE_DELETE;
import static org.radarcns.auth.authorization.Permission.SOURCETYPE_READ;
import static org.radarcns.auth.authorization.Permission.SOURCETYPE_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing SourceType.
 */
@RestController
@RequestMapping("/api")
public class SourceTypeResource {

    private final Logger log = LoggerFactory.getLogger(SourceTypeResource.class);

    private static final String ENTITY_NAME = "sourceType";

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /source-types : Create a new sourceType.
     *
     * @param sourceTypeDTO the sourceTypeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceTypeDTO, or
     * with status 400 (Bad Request) if the sourceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-types")
    @Timed
    public ResponseEntity<SourceTypeDTO> createSourceType(@Valid @RequestBody
            SourceTypeDTO sourceTypeDTO) throws URISyntaxException {
        log.debug("REST request to save SourceType : {}", sourceTypeDTO);
        checkPermission(getJWT(servletRequest), SOURCETYPE_CREATE);
        if (sourceTypeDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "A new sourceType cannot already have an ID")).build();
        }
        Optional<SourceType> existing = sourceTypeRepository
                .findOneWithEagerRelationshipsByProducerAndModelAndVersion(
                        sourceTypeDTO.getProducer(), sourceTypeDTO.getModel(),
                        sourceTypeDTO.getCatalogVersion());

        if (existing.isPresent()) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "A SourceType with the specified producer, model and "
                    + "version already exists. This combination needs to be unique.");
            errorParams.put("producer", sourceTypeDTO.getProducer());
            errorParams.put("model", sourceTypeDTO.getModel());
            errorParams.put("catalogVersion", sourceTypeDTO.getCatalogVersion());
            throw new CustomConflictException(ErrorConstants.ERR_SOURCE_TYPE_EXISTS, errorParams,
                    new URI(HeaderUtil.buildPath("api", "source-types",
                            sourceTypeDTO.getProducer(), sourceTypeDTO.getModel(),
                            sourceTypeDTO.getCatalogVersion())));
        }
        SourceTypeDTO result = sourceTypeService.save(sourceTypeDTO);
        return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "source-types",
                result.getProducer(), result.getModel(), result.getCatalogVersion())))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, displayName(result)))
                .body(result);
    }

    /**
     * PUT  /source-types : Updates an existing sourceType.
     *
     * @param sourceTypeDTO the sourceTypeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceTypeDTO,
     * or with status 400 (Bad Request) if the sourceTypeDTO is not valid,
     * or with status 500 (Internal Server Error) if the sourceTypeDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-types")
    @Timed
    public ResponseEntity<SourceTypeDTO> updateSourceType(@Valid @RequestBody
            SourceTypeDTO sourceTypeDTO) throws URISyntaxException {
        log.debug("REST request to update SourceType : {}", sourceTypeDTO);
        if (sourceTypeDTO.getId() == null) {
            return createSourceType(sourceTypeDTO);
        }
        checkPermission(getJWT(servletRequest), SOURCETYPE_UPDATE);
        SourceTypeDTO result = sourceTypeService.save(sourceTypeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, displayName(sourceTypeDTO)))
            .body(result);
    }

    /**
     * GET  /source-types : get all the sourceTypes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sourceTypes in body
     */
    @GetMapping("/source-types")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getAllSourceTypes() {
        checkPermission(getJWT(servletRequest), SOURCETYPE_READ);
        return ResponseEntity.ok(sourceTypeService.findAll());
    }

    /**
     * Find the list of SourceTypes made by the given producer
     * @param producer The producer
     * @return A list of objects matching the producer
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getSourceTypes(@PathVariable String producer) {
        checkPermission(getJWT(servletRequest), SOURCETYPE_READ);
        return ResponseEntity.ok(sourceTypeService.findByProducer(producer));
    }

    /**
     * Find the list of SourceTypes of the given producer and model. Can be multiple since
     * multiple version of a single model can be made.
     *
     * @param producer The producer
     * @param model The model
     * @return A list of objects matching the producer and model
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
            + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getSourceTypes(@PathVariable String producer,
            @PathVariable String model) {
        checkPermission(getJWT(servletRequest), SOURCETYPE_READ);
        return ResponseEntity.ok(sourceTypeService.findByProducerAndModel(producer, model));
    }

    /**
     * Find the SourceType of the given producer, model and version
     *
     * @param producer The producer
     * @param model The model
     * @param version The version
     * @return A single SourceType object matching the producer, model and version
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
            + Constants.ENTITY_ID_REGEX + "}/{version:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SourceTypeDTO> getSourceTypes(@PathVariable String producer,
        @PathVariable String model, @PathVariable String version) {
        checkPermission(getJWT(servletRequest), SOURCETYPE_READ);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(
            sourceTypeService.findByProducerAndModelAndVersion(producer, model, version)));
    }

    /**
     * DELETE  /source-types/:producer/:model/:version : delete the sourceType with the specified
     * producer, model and version
     *
     * @param producer The producer
     * @param model The model
     * @param version The version
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
            + Constants.ENTITY_ID_REGEX + "}/{version:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSourceType(@PathVariable String producer,
        @PathVariable String model, @PathVariable String version) {
        checkPermission(getJWT(servletRequest), SOURCETYPE_DELETE);
        SourceTypeDTO sourceTypeDTO = sourceTypeService
            .findByProducerAndModelAndVersion(producer, model, version);
        if (Objects.isNull(sourceTypeDTO)) {
            return ResponseEntity.notFound().build();
        }
        List<ProjectDTO> projects = sourceTypeService.findProjectsBySourceType(producer, model,
                version);
        if (!projects.isEmpty()) {
            throw new CustomParameterizedException(ErrorConstants.ERR_SOURCE_TYPE_IN_USE,
                    projects.stream()
                            .map(p -> p.getProjectName())
                            .reduce((s1, s2) -> String.join(", ", s1, s2))
                            .get()); // we know the list is not empty so calling get() is safe here
        }
        sourceTypeService.delete(sourceTypeDTO.getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
                displayName(sourceTypeDTO))).build();
    }

    private String displayName(SourceTypeDTO sourceType) {
        return String.join(" ", sourceType.getProducer(), sourceType.getModel(),
                sourceType.getCatalogVersion());
    }
}
