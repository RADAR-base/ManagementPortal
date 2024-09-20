package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.radarbase.management.domain.Source
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.service.mapper.decorator.SourceMapperDecorator

/**
 * Mapper for the entity Source and its DTO SourceDTO.
 */
@Mapper(componentModel = "spring", uses = [SourceTypeMapper::class, ProjectMapper::class])
@DecoratedWith(
    SourceMapperDecorator::class,
)
interface SourceMapper {
    @Mapping(source = "source.subject.user.login", target = "subjectLogin")
    fun sourceToSourceDTO(source: Source): SourceDTO

    @Named("sourceWithoutProjectDTO")
    @Mapping(source = "source.subject.user.login", target = "subjectLogin")
    @Mapping(target = "project", ignore = true)
    fun sourceToSourceWithoutProjectDTO(source: Source): SourceDTO

    @IterableMapping(qualifiedByName = ["sourceWithoutProjectDTO"])
    fun sourcesToSourceDTOs(sources: List<Source>): List<SourceDTO>

    @Mapping(source = "sourceType.id", target = "sourceTypeId")
    @Mapping(source = "sourceType.producer", target = "sourceTypeProducer")
    @Mapping(source = "sourceType.model", target = "sourceTypeModel")
    @Mapping(source = "sourceType.catalogVersion", target = "sourceTypeCatalogVersion")
    @Mapping(source = "assigned", target = "assigned")
    fun sourceToMinimalSourceDetailsDTO(source: Source): MinimalSourceDetailsDTO

    fun sourcesToMinimalSourceDetailsDTOs(sources: List<Source>): List<MinimalSourceDetailsDTO>

    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    fun minimalSourceDTOToSource(minimalSourceDetailsDto: MinimalSourceDetailsDTO): Source?

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    fun sourceDTOToSource(sourceDto: SourceDTO): Source
}
