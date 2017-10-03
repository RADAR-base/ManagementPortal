package org.radarcns.management.service.mapper;

import org.radarcns.management.domain.*;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;

import org.mapstruct.*;
import java.util.List;
import org.radarcns.management.service.dto.SourceRegistrationDTO;
import org.radarcns.management.service.mapper.decorator.SourceMapperDecorator;

/**
 * Mapper for the entity Source and its DTO SourceDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviceTypeMapper.class, ProjectMapper.class})
@DecoratedWith(SourceMapperDecorator.class)
public interface SourceMapper {

//    @Mapping(source = "deviceType.id", target = "deviceTypeId")
//    @Mapping(source = "project", target = "project")
    SourceDTO sourceToSourceDTO(Source source);

    @Mapping(source = "deviceType.id", target = "deviceType")
    @Mapping(source = "sourceId", target = "sourceId")
    @Mapping(target = "deviceTypeName", ignore = true)
    @Mapping(source = "expectedSourceName", target = "expectedSourceName")
    @Mapping(source = "sourceName", target = "sourceName")
    MinimalSourceDetailsDTO sourceToMinimalSourceDetailsDTO(Source source);

    List<MinimalSourceDetailsDTO> sourcesToMinimalSourceDetailsDTOs(List<Source> sources);

    Source descriptiveDTOToSource(MinimalSourceDetailsDTO minimalSourceDetailsDTO);

    List<Source> descriptiveDTOsToSources(List<MinimalSourceDetailsDTO> minimalSourceDetailsDTOS);

    List<SourceDTO> sourcesToSourceDTOs(List<Source> sources);

    List<SourceRegistrationDTO> sourcesToSourceRegisterationDTOs(List<Source> sources);

    @Mapping(source = "sourceId" , target = "sourceId")
    @Mapping(source = "expectedSourceName" , target = "expectedSourceName")
    @Mapping(source = "deviceType.deviceModel" , target = "deviceTypeModel")
    @Mapping(source = "deviceType.deviceProducer" , target = "deviceTypeProducer")
    @Mapping(source = "deviceType.catalogVersion" , target = "deviceCatalogVersion")
    @Mapping(source = "project.id" , target = "projectId")
    @Mapping(source = "assigned" , target = "assigned")
    SourceRegistrationDTO sourceToSourceRegistrationDTO(Source source);

//    @Mapping(source = "deviceTypeId", target = "deviceType")
//    @Mapping(source = "project", target = "project")
    @Mapping(target = "subjects" , ignore = false)
    Source sourceDTOToSource(SourceDTO sourceDTO);

    List<Source> sourceDTOsToSources(List<SourceDTO> sourceDTOS);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default Source sourceFromId(Long id) {
        if (id == null) {
            return null;
        }
        Source source = new Source();
        source.setId(id);
        return source;
    }


}
