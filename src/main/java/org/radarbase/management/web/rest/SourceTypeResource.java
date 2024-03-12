package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.repository.SourceTypeRepository;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.SourceTypeService;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.InvalidRequestException;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.radarbase.auth.authorization.Permission.SOURCETYPE_CREATE;
import static org.radarbase.auth.authorization.Permission.SOURCETYPE_DELETE;
import static org.radarbase.auth.authorization.Permission.SOURCETYPE_READ;
import static org.radarbase.auth.authorization.Permission.SOURCETYPE_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_TYPE;

/**
 * REST controller for managing SourceType.
 */
@RestController
@RequestMapping("/api")
public class SourceTypeResource {
    private static final Logger log = LoggerFactory.getLogger(SourceTypeResource.class);

    @Autowired
    private RadarToken token;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    /**
     * POST  /source-types : Create a new sourceType.
     *
     * @param sourceTypeDto the sourceTypeDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceTypeDto, or
     *     with status 400 (Bad Request) if the sourceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-types")
    @Timed
    public ResponseEntity<SourceTypeDTO> createSourceType(@Valid @RequestBody
            SourceTypeDTO sourceTypeDto) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save SourceType : {}", sourceTypeDto);
        checkPermission(token, SOURCETYPE_CREATE);
        if (sourceTypeDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(SOURCE_TYPE,
                    "idexists", "A new sourceType cannot already have an ID")).build();
        }
        Optional<SourceType> existing = sourceTypeRepository
                .findOneWithEagerRelationshipsByProducerAndModelAndVersion(
                        sourceTypeDto.getProducer(), sourceTypeDto.getModel(),
                        sourceTypeDto.getCatalogVersion());

        if (existing.isPresent()) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "A SourceType with the specified producer, model and "
                    + "version already exists. This combination needs to be unique.");
            errorParams.put("producer", sourceTypeDto.getProducer());
            errorParams.put("model", sourceTypeDto.getModel());
            errorParams.put("catalogVersion", sourceTypeDto.getCatalogVersion());
            throw new ConflictException("A SourceType with the specified producer, model and"
                + "version already exists. This combination needs to be unique.", SOURCE_TYPE,
                ErrorConstants.ERR_SOURCE_TYPE_EXISTS, errorParams);
        }
        SourceTypeDTO result = sourceTypeService.save(sourceTypeDto);
        return ResponseEntity.created(ResourceUriService.getUri(result))
                .headers(HeaderUtil.createEntityCreationAlert(SOURCE_TYPE, displayName(result)))
                .body(result);
    }

    /**
     * PUT  /source-types : Updates an existing sourceType.
     *
     * @param sourceTypeDto the sourceTypeDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceTypeDto, or
     *     with status 400 (Bad Request) if the sourceTypeDto is not valid, or with status 500
     *     (Internal Server Error) if the sourceTypeDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-types")
    @Timed
    public ResponseEntity<SourceTypeDTO> updateSourceType(@Valid @RequestBody
            SourceTypeDTO sourceTypeDto) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update SourceType : {}", sourceTypeDto);
        if (sourceTypeDto.getId() == null) {
            return createSourceType(sourceTypeDto);
        }
        checkPermission(token, SOURCETYPE_UPDATE);
        SourceTypeDTO result = sourceTypeService.save(sourceTypeDto);
        return ResponseEntity.ok()
                .headers(
                        HeaderUtil.createEntityUpdateAlert(SOURCE_TYPE, displayName(sourceTypeDto)))
                .body(result);
    }

    /**
     * GET  /source-types : get all the sourceTypes.
     *
     * @param pageable parameters
     * @return the ResponseEntity with status 200 (OK) and the list of sourceTypes in body
     */
    @GetMapping("/source-types")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getAllSourceTypes(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable)
            throws NotAuthorizedException {
        checkPermission(token, SOURCETYPE_READ);
        Page<SourceTypeDTO> page = sourceTypeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/source-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Find the list of SourceTypes made by the given producer.
     *
     * @param producer The producer
     * @return A list of objects matching the producer
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getSourceTypes(@PathVariable String producer)
            throws NotAuthorizedException {
        checkPermission(token, SOURCETYPE_READ);
        return ResponseEntity.ok(sourceTypeService.findByProducer(producer));
    }

    /**
     * Find the list of SourceTypes of the given producer and model. Can be multiple since multiple
     * version of a single model can be made.
     *
     * @param producer The producer
     * @param model The model
     * @return A list of objects matching the producer and model
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
            + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<List<SourceTypeDTO>> getSourceTypes(@PathVariable String producer,
            @PathVariable String model) throws NotAuthorizedException {
        checkPermission(token, SOURCETYPE_READ);
        return ResponseEntity.ok(sourceTypeService.findByProducerAndModel(producer, model));
    }

    /**
     * Find the SourceType of the given producer, model and version.
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
            @PathVariable String model, @PathVariable String version)
            throws NotAuthorizedException {
        checkPermission(token, SOURCETYPE_READ);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(
                sourceTypeService.findByProducerAndModelAndVersion(producer, model, version)));
    }

    /**
     * DELETE  /source-types/:producer/:model/:version : delete the sourceType with the specified
     * producer, model and version.
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
            @PathVariable String model, @PathVariable String version)
            throws NotAuthorizedException {
        checkPermission(token, SOURCETYPE_DELETE);
        SourceTypeDTO sourceTypeDto = sourceTypeService
                .findByProducerAndModelAndVersion(producer, model, version);
        if (Objects.isNull(sourceTypeDto)) {
            return ResponseEntity.notFound().build();
        }
        List<ProjectDTO> projects = sourceTypeService.findProjectsBySourceType(producer, model,
                version);
        if (!projects.isEmpty()) {
            throw new InvalidRequestException(
                // we know the list is not empty so calling get() is safe here
                "Cannot delete a source-type that " + "is being used by project(s)", SOURCE_TYPE,
                ErrorConstants.ERR_SOURCE_TYPE_IN_USE, Collections.singletonMap("project-names",
                projects
                        .stream()
                        .map(ProjectDTO::getProjectName)
                        .collect(Collectors.joining("-"))));
        }
        sourceTypeService.delete(sourceTypeDto.getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(SOURCE_TYPE,
                displayName(sourceTypeDto))).build();
    }

    private String displayName(SourceTypeDTO sourceType) {
        return String.join(" ", sourceType.getProducer(), sourceType.getModel(),
                sourceType.getCatalogVersion());
    }
}
