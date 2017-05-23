package org.radarcns.management.service.mapper;

import org.mapstruct.Mapper;
import org.radarcns.management.domain.SensorData;
import org.radarcns.management.service.dto.SensorDataDTO;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = { DeviceTypeMapper.class })
public interface SensorDataMapper {

    SensorData sensorDataDTOToSensorData(SensorDataDTO sensorDataDTO);

    SensorDataDTO sensorDataToSensorDataDTO(SensorData sensorData);
}
