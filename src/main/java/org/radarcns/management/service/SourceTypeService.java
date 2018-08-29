package org.radarcns.management.service;

import static org.radarcns.management.web.rest.errors.EntityName.SOURCE_TYPE;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_SOURCE_TYPE_NOT_FOUND;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import org.radarcns.management.domain.SourceData;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.repository.SourceTypeRepository;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing SourceType.
 */
@Service
@Transactional
public class SourceTypeService {

    private final Logger log = LoggerFactory.getLogger(SourceTypeService.class);

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

    @Autowired
    private SourceDataRepository sourceDataRepository;

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
        for (SourceData data : sourceType.getSourceData()) {
            data.setSourceType(sourceType);
        }
        sourceType = sourceTypeRepository.save(sourceType);
        sourceDataRepository.save(sourceType.getSourceData());
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
        return result.stream().map(sourceTypeMapper::sourceTypeToSourceTypeDTO)
                .collect(Collectors.toCollection(LinkedList::new));

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
        sourceTypeRepository.delete(id);
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
        List<SourceTypeDTO> sourceTypeDtos = sourceTypeMapper.sourceTypesToSourceTypeDTOs(
                sourceTypes);
        return sourceTypeDtos;
    }

    /**
     * Fetch SourceType by producer and model.
     */
    public List<SourceTypeDTO> findByProducerAndModel(String producer, String model) {
        log.debug("Request to get SourceType by producer and model: {}, {}", producer, model);
        List<SourceType> sourceTypes = sourceTypeRepository
                .findWithEagerRelationshipsByProducerAndModel(producer, model);
        List<SourceTypeDTO> sourceTypeDtos = sourceTypeMapper.sourceTypesToSourceTypeDTOs(
                sourceTypes);
        return sourceTypeDtos;
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
}
