import org.radarbase.management.service.dto.QueryContentDTO

class QueryContentGroupDTO(
    val contentGroupName: String?,
    val queryGroupId: Long?,
    val queryContentDTOList: List<QueryContentDTO>?,
    val id: Long?
)
