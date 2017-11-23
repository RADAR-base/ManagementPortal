package org.radarcns.management.service;

import org.radarcns.management.domain.SourceData;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.repository.SourceTypeRepository;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing SourceType.
 */
@Service
@Transactional
public class SourceTypeService {

    private final Logger log = LoggerFactory.getLogger(SourceTypeService.class);

    @Autowired
    private  SourceTypeRepository sourceTypeRepository;

    @Autowired
    private  SourceTypeMapper sourceTypeMapper;

    @Autowired
    private SourceDataRepository sourceDataRepository;

    /**
     * Save a sourceType.
     *
     * @param sourceTypeDTO the entity to save
     * @return the persisted entity
     */
    public SourceTypeDTO save(SourceTypeDTO sourceTypeDTO) {
        log.debug("Request to save SourceType : {}", sourceTypeDTO);
        SourceType sourceType = sourceTypeMapper.sourceTypeDTOToSourceType(sourceTypeDTO);
        // populate the SourceType of our SourceData's
        for (SourceData data : sourceType.getSourceData()) {
            data.setSourceType(sourceType);
        }
        sourceType = sourceTypeRepository.save(sourceType);
        sourceDataRepository.save(sourceType.getSourceData());
        return sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);
    }

    /**
     *  Get all the sourceTypes.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceTypeDTO> findAll() {
        log.debug("Request to get all SourceTypes");
        List<SourceType> result = sourceTypeRepository.findAllWithEagerRelationships();
        return result.stream()
            .map(sourceTypeMapper::sourceTypeToSourceTypeDTO)
            .collect(Collectors.toCollection(LinkedList::new));

    }

    /**
     *  Get one sourceType by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SourceTypeDTO findOne(Long id) {
        log.debug("Request to get SourceType : {}", id);
        SourceType sourceType = sourceTypeRepository.findOneWithEagerRelationships(id);
        return sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);
    }

    /**
     *  Delete the  sourceType by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SourceType : {}", id);
        sourceTypeRepository.delete(id);
    }

    /**
     * Fetch SourceType by producer and model
     */
    public SourceTypeDTO findByProducerAndModelAndVersion(String producer, String model , String version) {
        log.debug("Request to get SourceType by producer and model and version: {}, {}, {}",
            producer, model, version);
        Optional<SourceType> sourceType = sourceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(producer, model, version);
        return sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType.orElse(null));
    }

    /**
     * Fetch SourceType by producer
     */
    public List<SourceTypeDTO> findByProducer(String producer) {
        log.debug("Request to get SourceType by producer: {}", producer);
        List<SourceType> sourceTypes = sourceTypeRepository
            .findWithEagerRelationshipsByProducer(producer);
        List<SourceTypeDTO> sourceTypeDTOs = sourceTypeMapper.sourceTypesToSourceTypeDTOs(
            sourceTypes);
        return sourceTypeDTOs;
    }

    /**
     * Fetch SourceType by producer and model
     */
    public List<SourceTypeDTO> findByProducerAndModel(String producer, String model) {
        log.debug("Request to get SourceType by producer and model: {}, {}", producer, model);
        List<SourceType> sourceTypes = sourceTypeRepository
            .findWithEagerRelationshipsByProducerAndModel(producer, model);
        List<SourceTypeDTO> sourceTypeDTOs = sourceTypeMapper.sourceTypesToSourceTypeDTOs(
            sourceTypes);
        return sourceTypeDTOs;
    }
}
