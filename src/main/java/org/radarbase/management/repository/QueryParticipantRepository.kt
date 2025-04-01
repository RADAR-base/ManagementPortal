package org.radarbase.management.repository

import org.radarbase.management.domain.QueryParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import javax.transaction.Transactional

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryParticipant::class, idClass = Long::class)
interface QueryParticipantRepository : JpaRepository<QueryParticipant, Long>  {

    fun findBySubjectId(Subject_id: Long): List<QueryParticipant>

    @Modifying
    @Transactional
    @Query(value = "delete from query_participant where subject_id = :Subject_id and query_group_id = :query_group_id;",nativeQuery = true)
    fun deleteByQueryGroup(Subject_id: Long, query_group_id: Long)

}
