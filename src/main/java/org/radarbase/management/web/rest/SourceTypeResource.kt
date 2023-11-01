package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.SourceType
import org.radarbase.management.repository.SourceTypeRepository
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ResourceUriService.getUri
import org.radarbase.management.service.SourceTypeService
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.web.rest.errors.ConflictException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.radarbase.management.web.rest.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.jhipster.web.util.ResponseUtil
import java.net.URISyntaxException
import java.util.*
import java.util.stream.Collectors
import javax.validation.Valid

/**
 * REST controller for managing SourceType.
 */
@RestController
@RequestMapping("/api")
class SourceTypeResource(
    @Autowired private val sourceTypeService: SourceTypeService,
    @Autowired private val sourceTypeRepository: SourceTypeRepository,
    @Autowired private val authService: AuthService
) {

    /**
     * POST  /source-types : Create a new sourceType.
     *
     * @param sourceTypeDto the sourceTypeDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceTypeDto, or
     * with status 400 (Bad Request) if the sourceType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-types")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createSourceType(@RequestBody @Valid sourceTypeDto: SourceTypeDTO?): ResponseEntity<SourceTypeDTO> {
        log.debug("REST request to save SourceType : {}", sourceTypeDto)
        authService.checkPermission(Permission.SOURCETYPE_CREATE)
        if (sourceTypeDto!!.id != null) {
            return ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    EntityName.SOURCE_TYPE,
                    "idexists", "A new sourceType cannot already have an ID"
                )
            ).build()
        }
        val existing: SourceType? = sourceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(
                sourceTypeDto.producer!!, sourceTypeDto.model!!,
                sourceTypeDto.catalogVersion!!
            )
        if (existing != null) {
            val errorParams: MutableMap<String, String?> = HashMap()
            errorParams["message"] = ("A SourceType with the specified producer, model and "
                    + "version already exists. This combination needs to be unique.")
            errorParams["producer"] = sourceTypeDto.producer
            errorParams["model"] = sourceTypeDto.model
            errorParams["catalogVersion"] = sourceTypeDto.catalogVersion
            throw ConflictException(
                "A SourceType with the specified producer, model and"
                        + "version already exists. This combination needs to be unique.", EntityName.SOURCE_TYPE,
                ErrorConstants.ERR_SOURCE_TYPE_EXISTS, errorParams
            )
        }
        val result = sourceTypeService.save(sourceTypeDto)
        return ResponseEntity.created(getUri(result))
            .headers(HeaderUtil.createEntityCreationAlert(EntityName.SOURCE_TYPE, displayName(result)))
            .body(result)
    }

    /**
     * PUT  /source-types : Updates an existing sourceType.
     *
     * @param sourceTypeDto the sourceTypeDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceTypeDto, or
     * with status 400 (Bad Request) if the sourceTypeDto is not valid, or with status 500
     * (Internal Server Error) if the sourceTypeDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-types")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateSourceType(@RequestBody @Valid sourceTypeDto: SourceTypeDTO?): ResponseEntity<SourceTypeDTO> {
        log.debug("REST request to update SourceType : {}", sourceTypeDto)
        if (sourceTypeDto!!.id == null) {
            return createSourceType(sourceTypeDto)
        }
        authService.checkPermission(Permission.SOURCETYPE_UPDATE)
        val result = sourceTypeService.save(sourceTypeDto)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(EntityName.SOURCE_TYPE, displayName(sourceTypeDto))
            )
            .body(result)
    }

    /**
     * GET  /source-types : get all the sourceTypes.
     *
     * @param pageable parameters
     * @return the ResponseEntity with status 200 (OK) and the list of sourceTypes in body
     */
    @GetMapping("/source-types")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getAllSourceTypes(
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable?
    ): ResponseEntity<List<SourceTypeDTO>> {
        authService.checkPermission(Permission.SOURCETYPE_READ)
        val page = sourceTypeService.findAll(pageable!!)
        val headers = PaginationUtil
            .generatePaginationHttpHeaders(page, "/api/source-types")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * Find the list of SourceTypes made by the given producer.
     *
     * @param producer The producer
     * @return A list of objects matching the producer
     */
    @GetMapping("/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSourceTypes(@PathVariable producer: String?): ResponseEntity<List<SourceTypeDTO>> {
        authService.checkPermission(Permission.SOURCETYPE_READ)
        return ResponseEntity.ok(sourceTypeService.findByProducer(producer!!))
    }

    /**
     * Find the list of SourceTypes of the given producer and model. Can be multiple since multiple
     * version of a single model can be made.
     *
     * @param producer The producer
     * @param model The model
     * @return A list of objects matching the producer and model
     */
    @GetMapping(
        "/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
                + Constants.ENTITY_ID_REGEX + "}"
    )
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSourceTypes(
        @PathVariable producer: String?,
        @PathVariable model: String?
    ): ResponseEntity<List<SourceTypeDTO>> {
        authService.checkPermission(Permission.SOURCETYPE_READ)
        return ResponseEntity.ok(
            sourceTypeService.findByProducerAndModel(
                producer!!, model!!
            )
        )
    }

    /**
     * Find the SourceType of the given producer, model and version.
     *
     * @param producer The producer
     * @param model The model
     * @param version The version
     * @return A single SourceType object matching the producer, model and version
     */
    @GetMapping(
        "/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
                + Constants.ENTITY_ID_REGEX + "}/{version:" + Constants.ENTITY_ID_REGEX + "}"
    )
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSourceTypes(
        @PathVariable producer: String?,
        @PathVariable model: String?, @PathVariable version: String?
    ): ResponseEntity<SourceTypeDTO> {
        authService.checkPermission(Permission.SOURCETYPE_READ)
        return ResponseUtil.wrapOrNotFound(
            Optional.ofNullable(
                sourceTypeService.findByProducerAndModelAndVersion(producer!!, model!!, version!!)
            )
        )
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
    @DeleteMapping(
        "/source-types/{producer:" + Constants.ENTITY_ID_REGEX + "}/{model:"
                + Constants.ENTITY_ID_REGEX + "}/{version:" + Constants.ENTITY_ID_REGEX + "}"
    )
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteSourceType(
        @PathVariable producer: String?,
        @PathVariable model: String?, @PathVariable version: String?
    ): ResponseEntity<Void> {
        authService.checkPermission(Permission.SOURCETYPE_DELETE)
        val sourceTypeDto = sourceTypeService
            .findByProducerAndModelAndVersion(producer!!, model!!, version!!)
        if (Objects.isNull(sourceTypeDto)) {
            return ResponseEntity.notFound().build()
        }
        val projects = sourceTypeService.findProjectsBySourceType(
            producer, model,
            version
        )
        if (!projects.isEmpty()) {
            throw InvalidRequestException( // we know the list is not empty so calling get() is safe here
                "Cannot delete a source-type that " + "is being used by project(s)", EntityName.SOURCE_TYPE,
                ErrorConstants.ERR_SOURCE_TYPE_IN_USE, Collections.singletonMap(
                    "project-names",
                    projects
                        .stream()
                        .map(ProjectDTO::projectName)
                        .collect(Collectors.joining("-"))
                )
            )
        }
        sourceTypeService.delete(sourceTypeDto.id!!)
        return ResponseEntity.ok().headers(
            HeaderUtil.createEntityDeletionAlert(
                EntityName.SOURCE_TYPE,
                displayName(sourceTypeDto)
            )
        ).build()
    }

    private fun displayName(sourceType: SourceTypeDTO?): String {
        return java.lang.String.join(
            " ", sourceType!!.producer, sourceType.model,
            sourceType.catalogVersion
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceTypeResource::class.java)
    }
}
