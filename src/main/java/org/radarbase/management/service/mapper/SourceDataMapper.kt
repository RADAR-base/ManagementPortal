package org.radarbase.management.service.mapper

import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.radarbase.management.domain.SourceData
import org.radarbase.management.service.dto.SourceDataDTO

/**
 * Mapper for the entity SourceData and its DTO SourceDataDTO.
 */
@Mapper(componentModel = "spring", uses = [SourceTypeMapper::class])
interface SourceDataMapper {
    @Named("sourceDataDTO")
    fun sourceDataToSourceDataDTO(sourceData: SourceData?): SourceDataDTO?

    @Named("sourceDataReducedDTO")
    @Mapping(target = "sourceType", ignore = true)
    fun sourceDataToSourceDataDTOReduced(sourceData: SourceData): SourceDataDTO

    @IterableMapping(qualifiedByName = ["sourceDataReducedDTO"])
    fun sourceDataToSourceDataDTOs(sourceData: List<SourceData>): List<SourceDataDTO>
    fun sourceDataDTOToSourceData(sourceDataDto: SourceDataDTO): SourceData
    fun sourceDataDTOsToSourceData(sourceDataDtos: List<SourceDataDTO>): List<SourceData>

    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity.
     *
     * @param id id of the entity
     * @return the entity instance
     */
    fun sourceDataFromId(id: Long?): SourceData? {
        if (id == null) {
            return null
        }
        val sourceData = SourceData()
        sourceData.id = id
        return sourceData
    }
}
