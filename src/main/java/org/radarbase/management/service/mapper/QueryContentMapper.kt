package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.SourceData
import org.radarbase.management.service.catalog.CatalogSourceData
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.mapper.decorator.ProjectMapperDecorator
import org.radarbase.management.service.mapper.decorator.QueryContentDecorator
import java.util.*


@Mapper(componentModel = "spring", uses = [])
@DecoratedWith(
    QueryContentDecorator::class
)
interface QueryContentMapper{

    @Mapping(target = "imageBlob", ignore = true)
    fun queryContentToQueryContentDTO(queryContent: QueryContent?): QueryContentDTO?

    @Mapping(target = "imageBlob", ignore = true)
    fun queryContentDTOToQueryContent(dto: QueryContentDTO?): QueryContent?


}
