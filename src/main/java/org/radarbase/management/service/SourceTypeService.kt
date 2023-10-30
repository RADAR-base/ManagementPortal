package org.radarbase.management.service

import org.radarbase.management.domain.SourceType
import org.radarbase.management.repository.SourceDataRepository
import org.radarbase.management.repository.SourceTypeRepository
import org.radarbase.management.service.catalog.CatalogSourceData
import org.radarbase.management.service.catalog.CatalogSourceType
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.service.mapper.CatalogSourceDataMapper
import org.radarbase.management.service.mapper.CatalogSourceTypeMapper
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.service.mapper.SourceTypeMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.validation.constraints.NotNull

/**
 * Service Implementation for managing SourceType.
 */
@Service
@Transactional
open class SourceTypeService(
    @Autowired private val sourceTypeRepository: SourceTypeRepository,
    @Autowired private val sourceTypeMapper: SourceTypeMapper,
    @Autowired private val sourceDataRepository: SourceDataRepository,
    @Autowired private val catalogSourceTypeMapper: CatalogSourceTypeMapper,
    @Autowired private val catalogSourceDataMapper: CatalogSourceDataMapper,
    @Autowired private val projectMapper: ProjectMapper
) {

    /**
     * Save a sourceType.
     *
     * @param sourceTypeDto the entity to save
     * @return the persisted entity
     */
    fun save(sourceTypeDto: SourceTypeDTO): SourceTypeDTO {
        log.debug("Request to save SourceType : {}", sourceTypeDto)
        var sourceType = sourceTypeMapper.sourceTypeDTOToSourceType(sourceTypeDto)
        // populate the SourceType of our SourceData's
        for (data in sourceType.sourceData) {
            data.sourceType = sourceType
        }
        sourceType = sourceTypeRepository.save(sourceType)
        sourceDataRepository.saveAll(sourceType.sourceData)
        return sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)
    }

    /**
     * Get all the sourceTypes.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    open fun findAll(): List<SourceTypeDTO> {
        log.debug("Request to get all SourceTypes")
        val result = sourceTypeRepository.findAllWithEagerRelationships()
        return result
            .map { sourceType: SourceType -> sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType) }
            .toList()
    }

    /**
     * Get all sourceTypes with pagination.
     *
     * @param pageable params
     * @return the list of entities
     */
    fun findAll(pageable: Pageable): Page<SourceTypeDTO> {
        log.debug("Request to get SourceTypes")
        return sourceTypeRepository.findAll(pageable)
            .map { sourceType: SourceType -> sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType) }
    }

    /**
     * Delete the  sourceType by id.
     *
     * @param id the id of the entity
     */
    fun delete(id: Long) {
        log.debug("Request to delete SourceType : {}", id)
        sourceTypeRepository.deleteById(id)
    }

    /**
     * Fetch SourceType by producer and model.
     */
    fun findByProducerAndModelAndVersion(
        producer: @NotNull String,
        model: @NotNull String, version: @NotNull String
    ): SourceTypeDTO {
        log.debug(
            "Request to get SourceType by producer and model and version: {}, {}, {}",
            producer, model, version
        )
        return sourceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(producer, model, version)
            .let { sourceType: SourceType? -> sourceType?.let { sourceTypeMapper.sourceTypeToSourceTypeDTO(it) } }
            ?: throw NotFoundException(
                    "SourceType not found with producer, model, " + "version ", EntityName.Companion.SOURCE_TYPE,
                    ErrorConstants.ERR_SOURCE_TYPE_NOT_FOUND, Collections.singletonMap<String, String?>(
                        "producer-model-version",
                        "$producer-$model-$version"
                    )
                )
    }

    /**
     * Fetch SourceType by producer.
     */
    fun findByProducer(producer: String): List<SourceTypeDTO> {
        log.debug("Request to get SourceType by producer: {}", producer)
        val sourceTypes = sourceTypeRepository
            .findWithEagerRelationshipsByProducer(producer)
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(
            sourceTypes
        )
    }

    /**
     * Fetch SourceType by producer and model.
     */
    fun findByProducerAndModel(producer: String, model: String): List<SourceTypeDTO> {
        log.debug("Request to get SourceType by producer and model: {}, {}", producer, model)
        val sourceTypes = sourceTypeRepository
            .findWithEagerRelationshipsByProducerAndModel(producer, model)
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(
            sourceTypes
        )
    }

    /**
     * Find projects associated to a particular SourceType.
     *
     * @param producer the SourceType producer
     * @param model the SourceType model
     * @param version the SourceType catalogVersion
     * @return the list of projects associated with this SourceType
     */
    fun findProjectsBySourceType(producer: String, model: String, version: String): List<ProjectDTO> {
        return projectMapper.projectsToProjectDTOs(
            sourceTypeRepository
                .findProjectsBySourceType(producer, model, version)
        )
    }

    /**
     * Converts given [CatalogSourceType] to [SourceType] and saves it to the databse
     * after validations.
     * @param catalogSourceTypes list of source-type from catalogue-server.
     */
    @Transactional
    open fun saveSourceTypesFromCatalogServer(catalogSourceTypes: List<CatalogSourceType>) {
        for (catalogSourceType in catalogSourceTypes) {
            var sourceType = catalogSourceTypeMapper
                .catalogSourceTypeToSourceType(catalogSourceType)
            if (!isSourceTypeValid(sourceType)) {
                continue
            }

            // check whether a source-type is already available with given config
            if (sourceTypeRepository.hasOneByProducerAndModelAndVersion(
                    sourceType!!.producer!!, sourceType.model!!,
                    sourceType.catalogVersion!!
                )
            ) {
                // skip for existing source-types
                log.info(
                    "Source-type {} is already available ", sourceType.producer
                            + "_" + sourceType.model
                            + "_" + sourceType.catalogVersion
                )
            } else {
                try {
                    // create new source-type
                    sourceType = sourceTypeRepository.save(sourceType)

                    // create source-data for the new source-type
                    for (catalogSourceData in catalogSourceType.data!!) {
                        saveSourceData(sourceType, catalogSourceData)
                    }
                } catch (ex: RuntimeException) {
                    log.error("Failed to import source type {}", sourceType, ex)
                }
            }
        }
        log.info("Completed source-type import from catalog-server")
    }

    private fun saveSourceData(sourceType: SourceType?, catalogSourceData: CatalogSourceData?) {
        try {
            val sourceData = catalogSourceDataMapper
                .catalogSourceDataToSourceData(catalogSourceData)
            // sourceDataName should be unique
            // generated by combining sourceDataType and source-type configs
            sourceData!!.sourceDataName(
                sourceType!!.producer
                        + "_" + sourceType.model
                        + "_" + sourceType.catalogVersion
                        + "_" + sourceData.sourceDataType
            )
            sourceData.sourceType(sourceType)
            sourceDataRepository.save(sourceData)
        } catch (ex: RuntimeException) {
            log.error("Failed to import source data {}", catalogSourceData, ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceTypeService::class.java)
        private fun isSourceTypeValid(sourceType: SourceType?): Boolean {
            if (sourceType!!.producer == null) {
                log.warn(
                    "Catalog source-type {} does not have a vendor. "
                            + "Skipping importing this type", sourceType.name
                )
                return false
            }
            if (sourceType.model == null) {
                log.warn(
                    "Catalog source-type {} does not have a model. "
                            + "Skipping importing this type", sourceType.name
                )
                return false
            }
            if (sourceType.catalogVersion == null) {
                log.warn(
                    "Catalog source-type {} does not have a version. "
                            + "Skipping importing this type", sourceType.name
                )
                return false
            }
            return true
        }
    }
}
