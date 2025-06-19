package org.radarbase.management.service.dto


 class QueryGroupContentDTO {
     var queryGroupId: Long? = null
     var queryGroupName: String? = null
     var contentGroups: List<QueryContentGroupDTO> = mutableListOf()
 }
