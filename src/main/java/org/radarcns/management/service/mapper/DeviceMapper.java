package org.radarcns.management.service.mapper;

import java.util.Set;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.Device;
import org.radarcns.management.service.dto.DeviceDTO;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring" , uses = {DeviceTypeMapper.class, ProjectMapper.class, PatientMapper.class})
public interface DeviceMapper {

    Device deviceDTOToDevice(DeviceDTO deviceDTO);

    DeviceDTO deviceToDeviceDTO(Device device);

    Set<Device> deviceDTOsToDevices(Set<DeviceDTO> deviceDTOS);

    Set<DeviceDTO> devicesToDeviceDTOs(Set<Device> devices);
}
