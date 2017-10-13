package org.radarcns.management.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.dto.MinimalDeviceTypeDTO;

import java.util.List;

/**
 * Mapper for the entity DeviceType and its DTO DeviceTypeDTO.
 */
@Mapper(componentModel = "spring", uses = {SensorDataMapper.class, })
public interface DeviceTypeMapper {

    DeviceTypeDTO deviceTypeToDeviceTypeDTO(DeviceType deviceType);

    List<DeviceTypeDTO> deviceTypesToDeviceTypeDTOs(List<DeviceType> deviceTypes);

    @Mapping(target = "projects", ignore = true)
    DeviceType deviceTypeDTOToDeviceType(DeviceTypeDTO deviceTypeDTO);

    List<DeviceType> deviceTypeDTOsToDeviceTypes(List<DeviceTypeDTO> deviceTypeDTOs);

    MinimalDeviceTypeDTO deviceTypeToMinimalDeviceTypeDetailsDTO(DeviceType deviceType);

    List<MinimalDeviceTypeDTO> deviceTypesToMinimalDeviceTypeDetailsDTOs(List<DeviceType> deviceTypes);

    DeviceType minimalDTOToDeviceType(MinimalDeviceTypeDTO minimalDeviceTypeDetailsDTO);

    List<DeviceType> minimalDTOsToDeviceTypes(List<MinimalDeviceTypeDTO> minimalProjectDetailsDTOS);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default DeviceType deviceTypeFromId(Long id) {
        if (id == null) {
            return null;
        }
        DeviceType deviceType = new DeviceType();
        deviceType.setId(id);
        return deviceType;
    }


}
