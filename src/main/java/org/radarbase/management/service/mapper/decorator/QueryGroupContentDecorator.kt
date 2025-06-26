package org.radarbase.management.service.mapper.decorator


import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.service.dto.QueryGroupContentDTO
import org.radarbase.management.service.mapper.QueryContentGroupMapper
import org.radarbase.management.service.mapper.QueryGroupContentMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

abstract class QueryGroupContentDecorator : QueryGroupContentMapper {

    @Autowired
    @Qualifier("delegate") private lateinit var delegate: QueryGroupContentMapper
    @Autowired private var queryContentGroupMapper: QueryContentGroupMapper? = null

    override fun queryGroupToQueryContentGroupDTO(queryGroup: QueryGroup?): QueryGroupContentDTO? {
        if(queryGroup == null) {
            return null
        }
        val dto = QueryGroupContentDTO()
        dto.queryGroupId = queryGroup.id
        dto.queryGroupName = queryGroup.name

        for(queryContentGroup in queryGroup.queryContentGroups) {
                val queryContentGroupDTO = queryContentGroupMapper?.queryContentGroupToQueryContentGroupDTO(queryContentGroup)
                if(queryContentGroupDTO != null) {
                    dto.contentGroups += queryContentGroupDTO
                }
        }

        return dto
    }
}
