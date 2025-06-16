package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.QueryLogicOperator

class QueryGroupDTO {
    lateinit var name : String
    lateinit var description : String
}

data class QueryContentGroupResponseDTO(
    val contentGroupName: String?,
    val queryGroupId: Long?,
    val queryContentDTOList: List<QueryContentDTO>?
)

