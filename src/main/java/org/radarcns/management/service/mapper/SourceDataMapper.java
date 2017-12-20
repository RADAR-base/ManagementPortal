package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.SourceData;
import org.radarcns.management.service.dto.SourceDataDTO;

/**
 * Mapper for the entity SourceData and its DTO SourceDataDTO.
 */
@Mapper(componentModel = "spring", uses = {SourceTypeMapper.class})
public interface SourceDataMapper {

    SourceDataDTO sourceDataToSourceDataDTO(SourceData sourceData);

    List<SourceDataDTO> sourceDataToSourceDataDTOs(List<SourceData> sourceData);

    SourceData sourceDataDTOToSourceData(SourceDataDTO sourceDataDTO);

    List<SourceData> sourceDataDTOsToSourceData(List<SourceDataDTO> sourceDataDTOs);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default SourceData sourceDataFromId(Long id) {
        if (id == null) {
            return null;
        }
        SourceData sourceData = new SourceData();
        sourceData.setId(id);
        return sourceData;
    }
}
