package org.radarcns.management.service;

import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.SensorData;
import org.radarcns.management.repository.DeviceTypeRepository;
import org.radarcns.management.repository.SensorDataRepository;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.mapper.DeviceTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing DeviceType.
 */
@Service
@Transactional
public class DeviceTypeService {

    private final Logger log = LoggerFactory.getLogger(DeviceTypeService.class);

    @Autowired
    private  DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private  DeviceTypeMapper deviceTypeMapper;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    /**
     * Save a deviceType.
     *
     * @param deviceTypeDTO the entity to save
     * @return the persisted entity
     */
    public DeviceTypeDTO save(DeviceTypeDTO deviceTypeDTO) {
        log.debug("Request to save DeviceType : {}", deviceTypeDTO);
        DeviceType deviceType = deviceTypeMapper.deviceTypeDTOToDeviceType(deviceTypeDTO);
        List<SensorData> savedSensorData = sensorDataRepository.save(deviceType.getSensorData());
        deviceType.setSensorData(new HashSet<>(savedSensorData));
        deviceType = deviceTypeRepository.save(deviceType);
        DeviceTypeDTO result = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
        return result;
    }

    /**
     *  Get all the deviceTypes.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeDTO> findAll() {
        log.debug("Request to get all DeviceTypes");
        List<DeviceTypeDTO> result = deviceTypeRepository.findAllWithEagerRelationships().stream()
            .map(deviceTypeMapper::deviceTypeToDeviceTypeDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    /**
     *  Get one deviceType by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DeviceTypeDTO findOne(Long id) {
        log.debug("Request to get DeviceType : {}", id);
        DeviceType deviceType = deviceTypeRepository.findOneWithEagerRelationships(id);
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
        return deviceTypeDTO;
    }

    /**
     *  Delete the  deviceType by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete DeviceType : {}", id);
        deviceTypeRepository.delete(id);
    }

    /**
     * Fetch DeviceType by producer and model
     */
    public DeviceTypeDTO findByProducerAndModel(String producer, String model) {
        log.debug("Request to get DeviceType by producer and model: {}, {}", producer, model);
        DeviceType deviceType = deviceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModel(producer, model);
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
        return deviceTypeDTO;
    }

    /**
     * Fetch DeviceType by producer
     */
    public List<DeviceTypeDTO> findByProducer(String producer) {
        log.debug("Request to get DeviceType by producer: {}", producer);
        List<DeviceType> deviceTypes = deviceTypeRepository
            .findWithEagerRelationshipsByProducer(producer);
        List<DeviceTypeDTO> deviceTypeDTOs = deviceTypeMapper.deviceTypesToDeviceTypeDTOs(deviceTypes);
        return deviceTypeDTOs;
    }

    /**
     * Fetch DeviceType by producer and model
     */
    public List<DeviceTypeDTO> findByModel(String model) {
        log.debug("Request to get DeviceType by model: {}", model);
        List<DeviceType> deviceTypes = deviceTypeRepository.findWithEagerRelationshipsByModel(model);
        List<DeviceTypeDTO> deviceTypeDTOs = deviceTypeMapper.deviceTypesToDeviceTypeDTOs(deviceTypes);
        return deviceTypeDTOs;
    }
}
