package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.Source
import org.radarbase.management.repository.SourceRepository
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.SourceService
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.web.rest.util.HeaderUtil
import org.radarbase.management.web.rest.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.history.Revision
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
import jakarta.validation.Valid

/**
 * REST controller for managing Source.
 */
@RestController
@RequestMapping("/api")
class SourceResource(
    @Autowired private val sourceService: SourceService,
    @Autowired private val sourceRepository: SourceRepository,
    @Autowired private val authService: AuthService
) {

    /**
     * POST  /sources : Create a new source.
     *
     * @param sourceDto the sourceDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDto, or with
     * status 400 (Bad Request) if the source has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sources")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createSource(@RequestBody @Valid sourceDto: SourceDTO?): ResponseEntity<SourceDTO> {
        log.debug("REST request to save Source : {}", sourceDto)
        val project = sourceDto!!.project
        authService.checkPermission(Permission.SOURCE_CREATE, { e: EntityDetails ->
            if (project != null) {
                e.project(project.projectName)
            }
        })

        return if (sourceDto.id != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "idexists", "A new source cannot already have an ID"
                )
            ).build()
        }  else if (sourceDto.sourceId != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "sourceIdExists", "A new source cannot already have a Source ID"
                )
            ).build()
        } else if (sourceRepository.findOneBySourceName(sourceDto.sourceName) != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "sourceNameExists", "Source name already in use"
                )
            ).build()
        } else if (sourceDto.assigned == null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "sourceAssignedRequired", "A new source must have the 'assigned' field specified"
                )
            ).body(null)
        } else {
            val result = sourceService.save(sourceDto)
            ResponseEntity.created(ResourceUriService.getUri(result)).headers(
                    HeaderUtil.createEntityCreationAlert(
                        ENTITY_NAME, result.sourceName
                    )
                ).body(result)
        }
    }

    /**
     * PUT  /sources : Updates an existing source.
     *
     * @param sourceDto the sourceDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDto, or with
     * status 400 (Bad Request) if the sourceDto is not valid, or with status 500 (Internal
     * Server Error) if the sourceDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sources")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateSource(@RequestBody @Valid sourceDto: SourceDTO): ResponseEntity<SourceDTO> {
        log.debug("REST request to update Source : {}", sourceDto)
        if (sourceDto.id == null) {
            return createSource(sourceDto)
        }
        val project = sourceDto.project
        authService.checkPermission(Permission.SOURCE_UPDATE, { e: EntityDetails ->
            if (project != null) {
                e.project(project.projectName)
            }
        })
        val updatedSource: SourceDTO? = sourceService.updateSource(sourceDto)
        return ResponseEntity.ok().headers(
            HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sourceDto.sourceName)
        ).body(
            updatedSource
        )
    }

    /**
     * GET  /sources : get all the sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getAllSources(
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable?
    ): ResponseEntity<List<SourceDTO>> {
        authService.checkPermission(Permission.SUBJECT_READ)
        log.debug("REST request to get all Sources")
        val page = sourceService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sources")
        return ResponseEntity(page!!.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /sources/:sourceName : get the source with this sourceName.
     *
     * @param sourceName the name of the sourceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSource(@PathVariable sourceName: String): ResponseEntity<SourceDTO> {
        log.debug("REST request to get Source : {}", sourceName)
        authService.checkScope(Permission.SOURCE_READ)
        val source = sourceService.findOneByName(sourceName)
        if (source != null) {
            authService.checkPermission(Permission.SOURCE_READ, { e: EntityDetails ->
                if (source.project != null) {
                    e.project(source.project!!.projectName)
                }
                e.subject(source.subjectLogin).source(source.sourceName)
            })
        }
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(source))
    }

    /**
     * DELETE  /sources/:sourceName : delete the "id" source.
     *
     * @param sourceName the id of the sourceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteSource(@PathVariable sourceName: String): ResponseEntity<Void> {
        log.debug("REST request to delete Source : {}", sourceName)
        authService.checkScope(Permission.SOURCE_DELETE)
        val sourceDto = sourceService.findOneByName(sourceName)
            ?: return ResponseEntity.notFound().build()
        authService.checkPermission(Permission.SOURCE_DELETE, { e: EntityDetails ->
            if (sourceDto.project != null) {
                e.project(sourceDto.project!!.projectName);
            }
            e.subject(sourceDto.subjectLogin)
                .source(sourceDto.sourceName)
        })
        if (sourceDto.assigned == true) {
            return ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "sourceIsAssigned", "Cannot delete an assigned source"
                )
            ).build()
        }
        val sourceId = sourceDto.id
        val sourceHistory = sourceId?.let { sourceRepository.findRevisions(it) }
        val sources =
            sourceHistory?.mapNotNull { obj: Revision<Int, Source?> -> obj.entity }?.filter { it.assigned == true }
                ?.toList()
        if (sources?.isNotEmpty() == true) {
            val failureAlert = HeaderUtil.createFailureAlert(
                ENTITY_NAME, "sourceRevisionIsAssigned", "Cannot delete a previously assigned source"
            )
            return ResponseEntity.status(HttpStatus.CONFLICT).headers(failureAlert).build()
        }
        sourceId?.let { sourceService.delete(it) }
        return ResponseEntity.ok().headers(
            HeaderUtil.createEntityDeletionAlert(
                ENTITY_NAME, sourceName
            )
        ).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceResource::class.java)
        private const val ENTITY_NAME = "source"
    }
}
