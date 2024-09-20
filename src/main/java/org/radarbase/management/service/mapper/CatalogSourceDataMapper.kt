package org.radarbase.management.service.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.domain.SourceData
import org.radarbase.management.service.catalog.CatalogSourceData

@Mapper(componentModel = "spring")
interface CatalogSourceDataMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "type", target = "sourceDataType")
    @Mapping(target = "sourceDataName", ignore = true)
    @Mapping(source = "sampleRate.frequency", target = "frequency")
    @Mapping(target = "dataClass", ignore = true)
    @Mapping(source = "appProvider", target = "provider")
    @Mapping(target = "enabled", expression = "java(true)")
    @Mapping(target = "sourceType", ignore = true)
    fun catalogSourceDataToSourceData(catalogSourceData: CatalogSourceData?): SourceData?

    fun catalogSourceDataListToSourceDataList(catalogSourceType: List<CatalogSourceData?>?): List<SourceData?>?
}
