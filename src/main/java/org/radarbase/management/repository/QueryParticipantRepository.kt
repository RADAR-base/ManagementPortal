package org.radarbase.management.repository

import org.radarbase.management.domain.QueryParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.query.Param

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryParticipant::class, idClass = Long::class)
interface QueryParticipantRepository : JpaRepository<QueryParticipant, Long>  {

    fun findBySubjectId(subjectId: Long): List<QueryParticipant>

    fun findBySubjectIdAndQueryGroupId(subjectId: Long, queryGroupId: Long) : QueryParticipant

    fun existsByQueryGroupId(queryGroupId: Long): Boolean
}
