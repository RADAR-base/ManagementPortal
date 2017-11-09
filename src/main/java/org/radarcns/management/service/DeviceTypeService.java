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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        for(SensorData data : deviceType.getSensorData()) {
            sensorDataRepository.save(data);
        }
        deviceType = deviceTypeRepository.save(deviceType);
        return deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
    }

    /**
     *  Get all the deviceTypes.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeDTO> findAll() {
        log.debug("Request to get all DeviceTypes");
        return deviceTypeRepository.findAllWithEagerRelationships().stream()
            .map(deviceTypeMapper::deviceTypeToDeviceTypeDTO)
            .collect(Collectors.toCollection(LinkedList::new));

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
        return deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
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
    public DeviceTypeDTO findByProducerAndModelAndVersion(String producer, String model , String version) {
        log.debug("Request to get DeviceType by producer and model and version: {}, {}, {}",
            producer, model, version);
        Optional<DeviceType> deviceType = deviceTypeRepository
            .findOneWithEagerRelationshipsByProducerAndModelAndVersion(producer, model, version);
        return deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType.orElse(null));
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
    public List<DeviceTypeDTO> findByProducerAndModel(String producer, String model) {
        log.debug("Request to get DeviceType by producer and model: {}, {}", producer, model);
        List<DeviceType> deviceTypes = deviceTypeRepository
            .findWithEagerRelationshipsByProducerAndModel(producer, model);
        List<DeviceTypeDTO> deviceTypeDTOs = deviceTypeMapper.deviceTypesToDeviceTypeDTOs(deviceTypes);
        return deviceTypeDTOs;
    }
}
