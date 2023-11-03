package org.radarbase.management.service

import org.radarbase.management.domain.SourceData
import org.radarbase.management.repository.SourceDataRepository
import org.radarbase.management.service.dto.SourceDataDTO
import org.radarbase.management.service.mapper.SourceDataMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing SourceData.
 */
@Service
@Transactional
class SourceDataService(
    private val sourceDataRepository: SourceDataRepository,
    private val sourceDataMapper: SourceDataMapper
) {
    /**
     * Save a sourceData.
     *
     * @param sourceDataDto the entity to save
     * @return the persisted entity
     */
    fun save(sourceDataDto: SourceDataDTO?): SourceDataDTO? {
        log.debug("Request to save SourceData : {}", sourceDataDto)
        if (sourceDataDto?.sourceDataType == null) {
            throw BadRequestException(
                ErrorConstants.ERR_VALIDATION, EntityName.SOURCE_DATA,
                "Source Data must contain a type or a topic."
            )
        }
        var sourceData = sourceDataMapper.sourceDataDTOToSourceData(sourceDataDto)
        sourceData = sourceDataRepository.save(sourceData)
        return sourceDataMapper.sourceDataToSourceDataDTO(sourceData)
    }

    /**
     * Get all the sourceData.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(): List<SourceDataDTO?> {
        log.debug("Request to get all SourceData")
        return sourceDataRepository.findAll().stream()
            .map { sourceData: SourceData? -> sourceDataMapper.sourceDataToSourceDataDTO(sourceData) }
            .toList()
    }

    /**
     * Get all the sourceData with pagination.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable?): Page<SourceDataDTO?> {
        log.debug("Request to get all SourceData")
        return sourceDataRepository.findAll(pageable)
            .map { sourceData: SourceData? -> sourceDataMapper.sourceDataToSourceDataDTO(sourceData) }
    }

    /**
     * Get one sourceData by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): SourceDataDTO? {
        log.debug("Request to get SourceData : {}", id)
        val sourceData = sourceDataRepository.findById(id).get()
        return sourceDataMapper.sourceDataToSourceDataDTO(sourceData)
    }

    /**
     * Get one sourceData by name.
     *
     * @param sourceDataName the sourceDataType of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findOneBySourceDataName(sourceDataName: String?): SourceDataDTO? {
        log.debug("Request to get SourceData : {}", sourceDataName)
        return sourceDataRepository.findOneBySourceDataName(sourceDataName)
            .let { sourceData: SourceData? -> sourceDataMapper.sourceDataToSourceDataDTO(sourceData) }
    }

    /**
     * Delete the  sourceData by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    fun delete(id: Long?) {
        log.debug("Request to delete SourceData : {}", id)
        sourceDataRepository.deleteById(id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceDataService::class.java)
    }
}
