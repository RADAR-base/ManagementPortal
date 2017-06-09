package org.radarcns.management.service.mapper;

import org.radarcns.management.domain.*;
import org.radarcns.management.service.dto.SensorDataDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity SensorData and its DTO SensorDataDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface SensorDataMapper {

    SensorDataDTO sensorDataToSensorDataDTO(SensorData sensorData);

    List<SensorDataDTO> sensorDataToSensorDataDTOs(List<SensorData> sensorData);

    @Mapping(target = "deviceTypes", ignore = true)
    SensorData sensorDataDTOToSensorData(SensorDataDTO sensorDataDTO);

    List<SensorData> sensorDataDTOsToSensorData(List<SensorDataDTO> sensorDataDTOs);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */
     
    default SensorData sensorDataFromId(Long id) {
        if (id == null) {
            return null;
        }
        SensorData sensorData = new SensorData();
        sensorData.setId(id);
        return sensorData;
    }
    

}
