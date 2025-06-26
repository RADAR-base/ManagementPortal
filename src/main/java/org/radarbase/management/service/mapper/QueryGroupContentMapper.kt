package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.service.dto.QueryGroupContentDTO
import org.radarbase.management.service.mapper.decorator.QueryGroupContentDecorator

@Mapper(componentModel = "spring", uses = [])
@DecoratedWith(
    QueryGroupContentDecorator::class
)
interface QueryGroupContentMapper {
    @Mapping(target = "contentGroups", ignore = true)
    fun queryGroupToQueryContentGroupDTO(queryContent: QueryGroup?): QueryGroupContentDTO?
}
