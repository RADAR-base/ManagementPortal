package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.service.dto.MinimalSourceTypeDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;

/**
 * Mapper for the entity SourceType and its DTO SourceTypeDTO.
 */
@Mapper(componentModel = "spring", uses = {SourceDataMapper.class,})
public interface SourceTypeMapper {

    SourceTypeDTO sourceTypeToSourceTypeDTO(SourceType sourceType);

    List<SourceTypeDTO> sourceTypesToSourceTypeDTOs(List<SourceType> sourceTypes);

    @Mapping(target = "projects", ignore = true)
    SourceType sourceTypeDTOToSourceType(SourceTypeDTO sourceTypeDTO);

    List<SourceType> sourceTypeDTOsToSourceTypes(List<SourceTypeDTO> sourceTypeDTOs);

    MinimalSourceTypeDTO sourceTypeToMinimalSourceTypeDetailsDTO(SourceType sourceType);

    List<MinimalSourceTypeDTO> sourceTypesToMinimalSourceTypeDetailsDTOs(
            List<SourceType> sourceTypes);

    SourceType minimalDTOToSourceType(MinimalSourceTypeDTO minimalSourceTypeDetailsDTO);

    List<SourceType> minimalDTOsToSourceTypes(List<MinimalSourceTypeDTO> minimalProjectDetailsDTOS);

    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default SourceType sourceTypeFromId(Long id) {
        if (id == null) {
            return null;
        }
        SourceType sourceType = new SourceType();
        sourceType.setId(id);
        return sourceType;
    }


}
