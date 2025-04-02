package org.radarbase.management.repository

import org.radarbase.management.domain.QueryParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.query.Param

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryParticipant::class, idClass = Long::class)
interface QueryParticipantRepository : JpaRepository<QueryParticipant, Long>  {

    fun findBySubjectId(Subject_id: Long): List<QueryParticipant>

    @Query(value = "delete from query_participant where subject_id = :subjectID and query_group_id = :queryGroupID;",nativeQuery = true)
    fun deleteByQueryGroup(@Param("subjectID") subjectID: Long, @Param("queryGroupID") queryGroupID: Long)

}
