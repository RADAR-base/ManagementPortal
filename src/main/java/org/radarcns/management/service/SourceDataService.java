package org.radarcns.management.service;

import org.radarcns.management.domain.SourceData;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.management.service.mapper.SourceDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing SourceData.
 */
@Service
@Transactional
public class SourceDataService {

    private final Logger log = LoggerFactory.getLogger(SourceDataService.class);

    private final SourceDataRepository sourceDataRepository;

    private final SourceDataMapper sourceDataMapper;

    public SourceDataService(SourceDataRepository sourceDataRepository, SourceDataMapper sourceDataMapper) {
        this.sourceDataRepository = sourceDataRepository;
        this.sourceDataMapper = sourceDataMapper;
    }

    /**
     * Save a sourceData.
     *
     * @param sourceDataDTO the entity to save
     * @return the persisted entity
     */
    public SourceDataDTO save(SourceDataDTO sourceDataDTO) {
        log.debug("Request to save SourceData : {}", sourceDataDTO);
        SourceData sourceData = sourceDataMapper.sourceDataDTOToSourceData(sourceDataDTO);
        sourceData = sourceDataRepository.save(sourceData);
        SourceDataDTO result = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
        return result;
    }

    /**
     *  Get all the sourceData.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDataDTO> findAll() {
        log.debug("Request to get all SourceData");
        List<SourceDataDTO> result = sourceDataRepository.findAll().stream()
            .map(sourceDataMapper::sourceDataToSourceDataDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    /**
     *  Get one sourceData by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SourceDataDTO findOne(Long id) {
        log.debug("Request to get SourceData : {}", id);
        SourceData sourceData = sourceDataRepository.findOne(id);
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
        return sourceDataDTO;
    }

    /**
     *  Get one sourceData by name.
     *
     *  @param sensorName the sensorName of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SourceDataDTO> findOneBySensorName(String sensorName) {
        log.debug("Request to get SourceData : {}", sensorName);
        return sourceDataRepository.findOneBySensorName(sensorName)
            .map(sourceDataMapper::sourceDataToSourceDataDTO);
    }

    /**
     *  Delete the  sourceData by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SourceData : {}", id);
        sourceDataRepository.delete(id);
    }
}
