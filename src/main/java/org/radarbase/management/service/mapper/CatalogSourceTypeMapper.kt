package org.radarbase.management.service.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.domain.SourceType
import org.radarbase.management.service.catalog.CatalogSourceType

@Mapper(componentModel = "spring")
interface CatalogSourceTypeMapper {
    @Mapping(source = "vendor", target = "producer")
    @Mapping(source = "version", target = "catalogVersion")
    @Mapping(source = "doc", target = "description")
    @Mapping(source = "scope", target = "sourceTypeScope")
    @Mapping(target = "sourceData", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "canRegisterDynamically", ignore = true)
    fun catalogSourceTypeToSourceType(catalogSourceType: CatalogSourceType?): SourceType?
    fun catalogSourceTypesToSourceTypes(catalogSourceType: List<CatalogSourceType?>?): List<SourceType?>?
}
