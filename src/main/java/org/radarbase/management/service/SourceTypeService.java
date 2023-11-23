package org.radarbase.management.service;

import org.radarbase.management.domain.SourceData;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.repository.SourceDataRepository;
import org.radarbase.management.repository.SourceTypeRepository;
import org.radarbase.management.service.catalog.CatalogSourceData;
import org.radarbase.management.service.catalog.CatalogSourceType;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.service.mapper.CatalogSourceDataMapper;
import org.radarbase.management.service.mapper.CatalogSourceTypeMapper;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.service.mapper.SourceTypeMapper;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_TYPE;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SOURCE_TYPE_NOT_FOUND;

/**
 * Service Implementation for managing SourceType.
 */
@Service
@Transactional
public class SourceTypeService {

    private static final Logger log = LoggerFactory.getLogger(SourceTypeService.class);

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

    @Autowired
    private SourceDataRepository sourceDataRepository;

    @Autowired
    private CatalogSourceTypeMapper catalogSourceTypeMapper;

    @Autowired
    private CatalogSourceDataMapper catalogSourceDataMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * Save a sourceType.
     *
     * @param sourceTypeDto the entity to save
     * @return the persisted entity
     */
    public SourceTypeDTO save(SourceTypeDTO sourceTypeDto) {
        log.debug("Request to save SourceType : {}", sourceTypeDto);
        SourceType sourceType = sourceTypeMapper.sourceTypeDTOToSourceType(sourceTypeDto);
        // populate the SourceType of our SourceData's
        for (SourceData data : sourceType.sourceData) {
            data.sourceType = sourceType;
        }
        sourceType = sourceTypeRepository.save(sourceType);
        sourceDataRepository.saveAll(sourceType.sourceData);
        return sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);
    }

    /**
     * Get all the sourceTypes.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceTypeDTO> findAll() {
        log.debug("Request to get all SourceTypes");
        List<SourceType> result = sourceTypeRepository.findAllWithEagerRelationships();
        return result.stream()
                .map(sourceTypeMapper::sourceTypeToSourceTypeDTO)
                .toList();

    }

    /**
     * Get all sourceTypes with pagination.
     *
     * @param pageable params
     * @return the list of entities
     */
    public Page<SourceTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get SourceTypes");
        return sourceTypeRepository.findAll(pageable)
                .map(sourceTypeMapper::sourceTypeToSourceTypeDTO);
    }

    /**
     * Delete the  sourceType by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SourceType : {}", id);
        sourceTypeRepository.deleteById(id);
    }

    /**
     * Fetch SourceType by producer and model.
     */
    public SourceTypeDTO findByProducerAndModelAndVersion(@NotNull String producer,
            @NotNull String model, @NotNull String version) {
        log.debug("Request to get SourceType by producer and model and version: {}, {}, {}",
                producer, model, version);
        return sourceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(producer, model, version)
            .map(sourceTypeMapper::sourceTypeToSourceTypeDTO).orElseThrow(
                () -> new NotFoundException(
                    "SourceType not found with producer, model, " + "version ", SOURCE_TYPE,
                    ERR_SOURCE_TYPE_NOT_FOUND, Collections.singletonMap("producer-model-version",
                    producer + "-" + model + "-" + version)));
    }

    /**
     * Fetch SourceType by producer.
     */
    public List<SourceTypeDTO> findByProducer(String producer) {
        log.debug("Request to get SourceType by producer: {}", producer);
        List<SourceType> sourceTypes = sourceTypeRepository
                .findWithEagerRelationshipsByProducer(producer);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(
                sourceTypes);
    }

    /**
     * Fetch SourceType by producer and model.
     */
    public List<SourceTypeDTO> findByProducerAndModel(String producer, String model) {
        log.debug("Request to get SourceType by producer and model: {}, {}", producer, model);
        List<SourceType> sourceTypes = sourceTypeRepository
                .findWithEagerRelationshipsByProducerAndModel(producer, model);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(
                sourceTypes);
    }

    /**
     * Find projects associated to a particular SourceType.
     *
     * @param producer the SourceType producer
     * @param model the SourceType model
     * @param version the SourceType catalogVersion
     * @return the list of projects associated with this SourceType
     */
    public List<ProjectDTO> findProjectsBySourceType(String producer, String model, String
            version) {
        return projectMapper.projectsToProjectDTOs(sourceTypeRepository
                .findProjectsBySourceType(producer, model, version));
    }

    /**
     * Converts given {@link CatalogSourceType} to {@link SourceType} and saves it to the databse
     * after validations.
     * @param catalogSourceTypes list of source-type from catalogue-server.
     */
    @Transactional
    public void saveSourceTypesFromCatalogServer(List<CatalogSourceType> catalogSourceTypes) {
        for (CatalogSourceType catalogSourceType : catalogSourceTypes) {
            SourceType sourceType = catalogSourceTypeMapper
                    .catalogSourceTypeToSourceType(catalogSourceType);

            if (!isSourceTypeValid(sourceType)) {
                continue;
            }

            // check whether a source-type is already available with given config
            if (sourceTypeRepository.hasOneByProducerAndModelAndVersion(
                    sourceType.producer, sourceType.model,
                    sourceType.catalogVersion)) {
                // skip for existing source-types
                log.info("Source-type {} is already available ", sourceType.producer
                        + "_" + sourceType.model
                        + "_" + sourceType.catalogVersion);
            } else {
                try {
                    // create new source-type
                    sourceType = sourceTypeRepository.save(sourceType);

                    // create source-data for the new source-type
                    for (CatalogSourceData catalogSourceData : catalogSourceType.getData()) {
                        saveSourceData(sourceType, catalogSourceData);
                    }
                } catch (RuntimeException ex) {
                    log.error("Failed to import source type {}", sourceType, ex);
                }
            }
        }
        log.info("Completed source-type import from catalog-server");
    }

    private void saveSourceData(SourceType sourceType, CatalogSourceData catalogSourceData) {
        try {
            SourceData sourceData = catalogSourceDataMapper
                    .catalogSourceDataToSourceData(catalogSourceData);
            // sourceDataName should be unique
            // generated by combining sourceDataType and source-type configs
            sourceData.sourceDataName(sourceType.producer
                    + "_" + sourceType.model
                    + "_" + sourceType.catalogVersion
                    + "_" + sourceData.sourceDataType);
            sourceData.sourceType(sourceType);
            sourceDataRepository.save(sourceData);
        } catch (RuntimeException ex) {
            log.error("Failed to import source data {}", catalogSourceData, ex);
        }
    }

    private static boolean isSourceTypeValid(SourceType sourceType) {
        if (sourceType.producer == null) {
            log.warn("Catalog source-type {} does not have a vendor. "
                    + "Skipping importing this type", sourceType.name);
            return false;
        }

        if (sourceType.model == null) {
            log.warn("Catalog source-type {} does not have a model. "
                    + "Skipping importing this type", sourceType.name);
            return false;
        }

        if (sourceType.catalogVersion == null) {
            log.warn("Catalog source-type {} does not have a version. "
                    + "Skipping importing this type", sourceType.name);
            return false;
        }
        return true;
    }
}
