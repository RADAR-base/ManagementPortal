package org.radarbase.management.service.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.SourceData
import org.radarbase.management.service.catalog.CatalogSourceData
import org.radarbase.management.service.dto.QueryContentDTO


@Mapper(componentModel = "spring", uses = [])
interface QueryContentMapper{
    fun queryContentToQueryContentDTO(queryContent: QueryContent?): QueryContentDTO?
}
