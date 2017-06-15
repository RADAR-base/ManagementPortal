package org.radarcns.management.service.mapper.decorator;

import org.radarcns.management.domain.Device;
import org.radarcns.management.repository.DeviceRepository;
import org.radarcns.management.service.dto.DescriptiveDeviceDTO;
import org.radarcns.management.service.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nivethika on 13-6-17.
 */

public abstract class DeviceMapperDecorator implements DeviceMapper {

    private static final String SEPARATOR = "_: ";

    @Autowired
    @Qualifier("delegate")
    private DeviceMapper delegate;

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public DescriptiveDeviceDTO deviceToDescriptiveDeviceDTO(Device device) {
        DescriptiveDeviceDTO dto = delegate.deviceToDescriptiveDeviceDTO( device );
        dto.setDeviceTypeAndPhysicalId( device.getDeviceType().getDeviceModel() + SEPARATOR + device.getDevicePhysicalId());
        return dto;
    }

    @Override
    public Device descriptiveDTOToDevice(DescriptiveDeviceDTO descriptiveDeviceDTO) {
        Device device = deviceRepository.findOne(descriptiveDeviceDTO.getId());
        device.setAssigned(descriptiveDeviceDTO.isAssigned());
        return device;
    }
}
