package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ResourceUriService.getUri
import org.radarbase.management.service.SourceDataService
import org.radarbase.management.service.dto.SourceDataDTO
import org.radarbase.management.web.rest.errors.ConflictException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityCreationAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityDeletionAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityUpdateAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createFailureAlert
import org.radarbase.management.web.rest.util.PaginationUtil.generatePaginationHttpHeaders
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
import javax.validation.Valid

/**
 * REST controller for managing SourceData.
 */
@RestController
@RequestMapping("/api")
class SourceDataResource(
    @Autowired private val sourceDataService: SourceDataService,
    @Autowired private val authService: AuthService
) {

    /**
     * POST  /source-data : Create a new sourceData.
     *
     * @param sourceDataDto the sourceDataDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDataDto, or
     * with status 400 (Bad Request) if the sourceData has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/source-data")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createSourceData(@RequestBody @Valid sourceDataDto: SourceDataDTO): ResponseEntity<SourceDataDTO?> {
        log.debug("REST request to save SourceData : {}", sourceDataDto)
        authService.checkPermission(Permission.SOURCEDATA_CREATE)
        if (sourceDataDto.id != null) {
            return ResponseEntity.badRequest().headers(
                createFailureAlert(
                    EntityName.SOURCE_DATA,
                    "idexists", "A new sourceData cannot already have an ID"
                )
            ).build()
        }
        val name = sourceDataDto.sourceDataName
        if (sourceDataService.findOneBySourceDataName(name) != null) {
            throw ConflictException(
                "SourceData already available with source-name",
                EntityName.SOURCE_DATA, "error.sourceDataNameAvailable",
                Collections.singletonMap("sourceDataName", name)
            )
        }
        val result = sourceDataService.save(sourceDataDto)
        return ResponseEntity.created(getUri(sourceDataDto))
            .headers(createEntityCreationAlert(EntityName.SOURCE_DATA, name))
            .body(result)
    }

    /**
     * PUT  /source-data : Updates an existing sourceData.
     *
     * @param sourceDataDto the sourceDataDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDataDto, or
     * with status 400 (Bad Request) if the sourceDataDto is not valid, or with status 500
     * (Internal Server Error) if the sourceDataDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/source-data")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateSourceData(@RequestBody @Valid sourceDataDto: SourceDataDTO?): ResponseEntity<SourceDataDTO?> {
        log.debug("REST request to update SourceData : {}", sourceDataDto)
        if (sourceDataDto!!.id == null) {
            return createSourceData(sourceDataDto)
        }
        authService.checkPermission(Permission.SOURCEDATA_UPDATE)
        val result = sourceDataService.save(sourceDataDto)
        return ResponseEntity.ok()
            .headers(createEntityUpdateAlert(EntityName.SOURCE_DATA, sourceDataDto.sourceDataName))
            .body(result)
    }

    /**
     * GET  /source-data : get all the sourceData.
     *
     * @param pageable parameters
     * @return the ResponseEntity with status 200 (OK) and the list of sourceData in body
     */
    @GetMapping("/source-data")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getAllSourceData(
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable
    ): ResponseEntity<List<SourceDataDTO?>> {
        log.debug("REST request to get all SourceData")
        authService.checkScope(Permission.SOURCEDATA_READ)
        val page = sourceDataService.findAll(pageable)
        val headers = generatePaginationHttpHeaders(page, "/api/source-data")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /source-data/:sourceDataName : get the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDataDTO, or with
     * status 404 (Not Found)
     */
    @GetMapping("/source-data/{sourceDataName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSourceData(@PathVariable sourceDataName: String?): ResponseEntity<SourceDataDTO> {
        authService.checkScope(Permission.SOURCEDATA_READ)
        return ResponseUtil.wrapOrNotFound(
            Optional.ofNullable(
                sourceDataService
                    .findOneBySourceDataName(sourceDataName)
            )
        )
    }

    /**
     * DELETE  /source-data/:sourceDataName : delete the "sourceDataName" sourceData.
     *
     * @param sourceDataName the sourceDataName of the sourceDataDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/source-data/{sourceDataName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteSourceData(@PathVariable sourceDataName: String?): ResponseEntity<Void> {
        authService.checkPermission(Permission.SOURCEDATA_DELETE)
        val sourceDataDto = sourceDataService
            .findOneBySourceDataName(sourceDataName) ?: return ResponseEntity.notFound().build()
        sourceDataService.delete(sourceDataDto.id)
        return ResponseEntity.ok().headers(createEntityDeletionAlert(EntityName.SOURCE_DATA, sourceDataName)).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceDataResource::class.java)
    }
}
