import org.radarbase.management.service.dto.QueryContentDTO

data class QueryContentGroupDTO(
    val contentGroupName: String?,
    val queryGroupId: Long?,
    val queryContentDTOList: List<QueryContentDTO>?,
    val id: Long?
)
