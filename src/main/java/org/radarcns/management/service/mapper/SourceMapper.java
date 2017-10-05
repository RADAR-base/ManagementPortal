package org.radarcns.management.service.mapper;

import org.radarcns.management.domain.*;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;

import org.mapstruct.*;
import java.util.List;

import org.radarcns.management.service.mapper.decorator.SourceMapperDecorator;

/**
 * Mapper for the entity Source and its DTO SourceDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviceTypeMapper.class, ProjectMapper.class})
@DecoratedWith(SourceMapperDecorator.class)
public interface SourceMapper {
    SourceDTO sourceToSourceDTO(Source source);

    @Mapping(source = "deviceType.id", target = "deviceTypeId")
    @Mapping(source = "deviceType.deviceProducer", target = "deviceTypeProducer")
    @Mapping(source = "deviceType.deviceModel", target = "deviceTypeModel")
    @Mapping(source = "deviceType.catalogVersion", target = "deviceTypeCatalogVersion")
    MinimalSourceDetailsDTO sourceToMinimalSourceDetailsDTO(Source source);

    List<MinimalSourceDetailsDTO> sourcesToMinimalSourceDetailsDTOs(List<Source> sources);

    Source descriptiveDTOToSource(MinimalSourceDetailsDTO minimalSourceDetailsDTO);

    List<SourceDTO> sourcesToSourceDTOs(List<Source> sources);

    Source sourceDTOToSource(SourceDTO sourceDTO);

    List<Source> sourceDTOsToSources(List<SourceDTO> sourceDTOS);
}
