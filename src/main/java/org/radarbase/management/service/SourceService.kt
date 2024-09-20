package org.radarbase.management.service

import org.hibernate.id.IdentifierGenerator
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.Source
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.SourceRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.service.mapper.SourceMapper
import org.radarbase.management.service.mapper.SourceTypeMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing Source.
 */
@Service
@Transactional
class SourceService(
    @Autowired private val sourceRepository: SourceRepository,
    @Autowired private val sourceMapper: SourceMapper,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val sourceTypeMapper: SourceTypeMapper,
    @Autowired private val authService: AuthService,
) {
    /**
     * Save a Source.
     *
     * @param sourceDto the entity to save
     * @return the persisted entity
     */
    fun save(sourceDto: SourceDTO): SourceDTO {
        log.debug("Request to save Source : {}", sourceDto)
        var source = sourceMapper.sourceDTOToSource(sourceDto)
        source = sourceRepository.save(source)
        return sourceMapper.sourceToSourceDTO(source)
    }

    /**
     * Get all the Sources.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(): List<SourceDTO> =
        sourceRepository
            .findAll()
            .filterNotNull()
            .map { source: Source -> sourceMapper.sourceToSourceDTO(source) }
            .toList()

    /**
     * Get all the sourceData with pagination.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable?): Page<SourceDTO>? {
        log.debug("Request to get SourceData with pagination")
        // somehow the compiler does not understand what's going on here, so we suppress the warning
        @Suppress("UNNECESSARY_SAFE_CALL")
        return pageable?.let {
            it.let { it1 ->
                sourceRepository
                    .findAll(it1)
                    ?.map { source -> source?.let { it2 -> sourceMapper.sourceToSourceDTO(it2) } }
            }
        }
    }

    /**
     * Get one source by name.
     *
     * @param sourceName the name of the source
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findOneByName(sourceName: String): SourceDTO? {
        log.debug("Request to get Source : {}", sourceName)
        return sourceRepository
            .findOneBySourceName(sourceName)
            .let { source: Source? -> source?.let { sourceMapper.sourceToSourceDTO(it) } }
    }

    /**
     * Get one source by id.
     *
     * @param id the id of the source
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findOneById(id: Long): Optional<SourceDTO> {
        log.debug("Request to get Source by id: {}", id)
        return Optional
            .ofNullable(sourceRepository.findById(id).orElse(null))
            .map { source: Source? -> source?.let { sourceMapper.sourceToSourceDTO(it) } }
    }

    /**
     * Delete the  device by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    fun delete(id: Long) {
        log.info("Request to delete Source : {}", id)
        val sourceHistory = sourceRepository.findRevisions(id)
        val sources =
            sourceHistory.content
                .mapNotNull { obj -> obj.entity }
                .filter {
                    it.assigned
                        ?: false
                }.toList()
        if (sources.isEmpty()) {
            sourceRepository.deleteById(id)
        } else {
            val errorParams: MutableMap<String, String> = HashMap()
            errorParams["message"] = "Cannot delete source with sourceId "
            errorParams["id"] = id.toString()
            throw InvalidRequestException(
                "Cannot delete a source that was once assigned.",
                EntityName.SOURCE,
                "error.usedSourceDeletion",
                errorParams,
            )
        }
    }

    /**
     * Returns all sources by project in [SourceDTO] format.
     *
     * @return list of sources
     */
    fun findAllByProjectId(
        projectId: Long,
        pageable: Pageable,
    ): Page<SourceDTO> =
        sourceRepository
            .findAllSourcesByProjectId(pageable, projectId)
            .map { source -> sourceMapper.sourceToSourceWithoutProjectDTO(source) }

    /**
     * Returns all sources by project in [MinimalSourceDetailsDTO] format.
     *
     * @return list of sources
     */
    fun findAllMinimalSourceDetailsByProject(
        projectId: Long,
        pageable: Pageable,
    ): Page<MinimalSourceDetailsDTO> =
        sourceRepository
            .findAllSourcesByProjectId(pageable, projectId)
            .map { source: Source -> sourceMapper.sourceToMinimalSourceDetailsDTO(source) }

    /**
     * Returns list of not-assigned sources by project id.
     */
    fun findAllByProjectAndAssigned(
        projectId: Long?,
        assigned: Boolean,
    ): List<SourceDTO> =
        sourceMapper.sourcesToSourceDTOs(
            sourceRepository.findAllSourcesByProjectIdAndAssigned(projectId, assigned),
        )

    /**
     * Returns list of not-assigned sources by project id.
     */
    fun findAllMinimalSourceDetailsByProjectAndAssigned(
        projectId: Long?,
        assigned: Boolean,
    ): List<MinimalSourceDetailsDTO> =
        sourceRepository
            .findAllSourcesByProjectIdAndAssigned(projectId, assigned)
            .map { source -> sourceMapper.sourceToMinimalSourceDetailsDTO(source) }
            .toList()

    /**
     * This method does a safe update of source assigned to a subject. It will allow updates of
     * attributes only.
     *
     * @param sourceToUpdate source fetched from database
     * @param attributes     value to update
     * @return Updated [MinimalSourceDetailsDTO] of source
     */
    fun safeUpdateOfAttributes(
        sourceToUpdate: Source,
        attributes: Map<String, String>?,
    ): MinimalSourceDetailsDTO {
        // update source attributes
        val updatedAttributes: MutableMap<String, String> = HashMap()
        updatedAttributes.putAll(sourceToUpdate.attributes)
        updatedAttributes.putAll(attributes!!)
        sourceToUpdate.attributes = updatedAttributes
        // rest of the properties should not be updated from this request.
        return sourceMapper.sourceToMinimalSourceDetailsDTO(sourceRepository.save(sourceToUpdate))
    }

    /**
     * Updates a source.
     * Does not allow to transfer a source, if it is currently assigned.
     * Does not allow to transfer if new project does not have valid source-type.
     *
     * @param sourceDto source details to update.
     * @return updated source.
     */
    @Transactional
    @Throws(NotAuthorizedException::class)
    fun updateSource(sourceDto: SourceDTO): SourceDTO? {
        val existingSourceOpt = sourceDto.id?.let { sourceRepository.findById(it) } ?: return null

        val existingSource = existingSourceOpt.get()
        authService.checkPermission(Permission.SOURCE_UPDATE, { e: EntityDetails ->
            e.source = existingSource.sourceName
            if (existingSource.project != null) {
                e.project = existingSource.project?.projectName
            }
            if (existingSource.subject != null &&
                existingSource.subject!!.user != null
            ) {
                e.subject = existingSource.subject?.user?.login
            }
        })

        // if the source is being transferred to another project.
        if (existingSource.project?.id != sourceDto.project?.id) {
            if (existingSource.assigned!!) {
                throw InvalidRequestException(
                    "Cannot transfer an assigned source",
                    EntityName.SOURCE,
                    "error.sourceIsAssigned",
                )
            }

            // check whether source-type of the device is assigned to the new project
            // to be transferred.
            val sourceType =
                projectRepository
                    .findSourceTypeByProjectIdAndSourceTypeId(
                        sourceDto.project?.id,
                        existingSource.sourceType?.id,
                    )
                    ?: throw InvalidRequestException(
                        "Cannot transfer a source to a project which doesn't have compatible " +
                            "source-type",
                        IdentifierGenerator.ENTITY_NAME,
                        "error.invalidTransfer",
                    )

            // set old source-type, ensures compatibility
            sourceDto.sourceType = existingSource.sourceType?.let { sourceTypeMapper.sourceTypeToSourceTypeDTO(it) }
        }
        return save(sourceDto)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceService::class.java)
    }
}
