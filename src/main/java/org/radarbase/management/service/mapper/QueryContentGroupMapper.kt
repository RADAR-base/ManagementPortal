package org.radarbase.management.service.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.domain.QueryContentGroup
import org.radarbase.management.service.dto.QueryContentGroupDTO


@Mapper(componentModel = "spring", uses = [])
interface QueryContentGroupMapper {
    @Mapping(target = "queryGroupId", ignore = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "contentGroupName", target = "contentGroupName")
    fun queryContentGroupToQueryContentGroupDTO(queryContentGroup: QueryContentGroup?): QueryContentGroupDTO?
}
