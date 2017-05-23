package org.radarcns.management.service.mapper;

import java.util.Set;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.service.dto.DeviceTypeDTO;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = {SensorDataMapper.class, ProjectMapper.class})
public interface DeviceTypeMapper {

    DeviceType deviceTypeDTOToDeviceType(DeviceTypeDTO deviceTypeDTO);

    DeviceTypeDTO deviceTypeToDeviceTypeDTO(DeviceType deviceType);

    Set<DeviceType> deviceTypeDTOsToDeviceTypes(Set<DeviceTypeDTO> deviceTypeDTOs);

    Set<DeviceTypeDTO> deviceTypesToDeviceTypeDTOs(Set<DeviceType> deviceTypes);
}
