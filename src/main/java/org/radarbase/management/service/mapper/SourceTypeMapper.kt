package org.radarbase.management.service.mapper

import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.radarbase.management.domain.SourceData
import org.radarbase.management.domain.SourceType
import org.radarbase.management.service.dto.MinimalSourceTypeDTO
import org.radarbase.management.service.dto.SourceDataDTO
import org.radarbase.management.service.dto.SourceTypeDTO

/**
 * Mapper for the entity SourceType and its DTO SourceTypeDTO.
 */
@Mapper(componentModel = "spring", uses = [SourceDataMapper::class])
interface SourceTypeMapper {
    @Named("sourceTypeToSourceTypeDTO")
    fun sourceTypeToSourceTypeDTO(sourceType: SourceType): SourceTypeDTO

    @Named("sourceTypeToSourceTypeDTOReduced")
    @Mapping(target = "sourceData", ignore = true)
    @Mapping(target = "assessmentType", ignore = true)
    @Mapping(target = "appProvider", ignore = true)
    @Mapping(target = "description", ignore = true)
    fun sourceTypeToSourceTypeDTOReduced(sourceType: SourceType?): SourceTypeDTO?

    @IterableMapping(qualifiedByName = ["sourceTypeToSourceTypeDTOReduced"])
    fun sourceTypesToSourceTypeDTOs(sourceTypes: List<SourceType>): List<SourceTypeDTO>

    @Mapping(target = "projects", ignore = true)
    fun sourceTypeDTOToSourceType(sourceTypeDto: SourceTypeDTO): SourceType
    fun sourceTypeDTOsToSourceTypes(sourceTypeDtos: List<SourceTypeDTO>): List<SourceType>
    fun sourceTypeToMinimalSourceTypeDetailsDTO(sourceType: SourceType): MinimalSourceTypeDTO
    fun sourceTypesToMinimalSourceTypeDetailsDTOs(
        sourceTypes: List<SourceType>
    ): List<MinimalSourceTypeDTO>

    @IterableMapping(qualifiedByName = ["sourceDataReducedDTO"])
    fun map(sourceData: Set<SourceData?>?): Set<SourceDataDTO?>?

    @Mapping(target = "sourceTypeScope", ignore = true)
    @Mapping(target = "sourceData", ignore = true)
    @Mapping(target = "canRegisterDynamically", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "assessmentType", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "appProvider", ignore = true)
    fun minimalDTOToSourceType(minimalSourceTypeDetailsDto: MinimalSourceTypeDTO?): SourceType?
    fun minimalDTOsToSourceTypes(minimalProjectDetailsDtos: List<MinimalSourceTypeDTO?>?): List<SourceType?>?

    /**
     * Generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity.
     *
     * @param id id of the entity
     * @return the entity instance
     */
    fun sourceTypeFromId(id: Long?): SourceType? {
        if (id == null) {
            return null
        }
        val sourceType = SourceType()
        sourceType.id = id
        return sourceType
    }
}
