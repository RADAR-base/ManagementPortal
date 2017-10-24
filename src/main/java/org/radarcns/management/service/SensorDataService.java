package org.radarcns.management.service;

import org.radarcns.management.domain.SensorData;
import org.radarcns.management.repository.SensorDataRepository;
import org.radarcns.management.service.dto.SensorDataDTO;
import org.radarcns.management.service.mapper.SensorDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing SensorData.
 */
@Service
@Transactional
public class SensorDataService {

    private final Logger log = LoggerFactory.getLogger(SensorDataService.class);
    
    private final SensorDataRepository sensorDataRepository;

    private final SensorDataMapper sensorDataMapper;

    public SensorDataService(SensorDataRepository sensorDataRepository, SensorDataMapper sensorDataMapper) {
        this.sensorDataRepository = sensorDataRepository;
        this.sensorDataMapper = sensorDataMapper;
    }

    /**
     * Save a sensorData.
     *
     * @param sensorDataDTO the entity to save
     * @return the persisted entity
     */
    public SensorDataDTO save(SensorDataDTO sensorDataDTO) {
        log.debug("Request to save SensorData : {}", sensorDataDTO);
        SensorData sensorData = sensorDataMapper.sensorDataDTOToSensorData(sensorDataDTO);
        sensorData = sensorDataRepository.save(sensorData);
        SensorDataDTO result = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);
        return result;
    }

    /**
     *  Get all the sensorData.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SensorDataDTO> findAll() {
        log.debug("Request to get all SensorData");
        List<SensorDataDTO> result = sensorDataRepository.findAll().stream()
            .map(sensorDataMapper::sensorDataToSensorDataDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    /**
     *  Get one sensorData by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SensorDataDTO findOne(Long id) {
        log.debug("Request to get SensorData : {}", id);
        SensorData sensorData = sensorDataRepository.findOne(id);
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);
        return sensorDataDTO;
    }

    /**
     *  Get one sensorData by name.
     *
     *  @param sensorName the sensorName of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SensorDataDTO> findOneBySensorName(String sensorName) {
        log.debug("Request to get SensorData : {}", sensorName);
        return sensorDataRepository.findOneBySensorName(sensorName)
            .map(sensorDataMapper::sensorDataToSensorDataDTO);
    }

    /**
     *  Delete the  sensorData by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SensorData : {}", id);
        sensorDataRepository.delete(id);
    }
}
