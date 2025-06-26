package org.radarbase.management.repository

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryEvaluation
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryContent::class, idClass = Long::class)
interface QueryContentRepository : JpaRepository<QueryContent, Long> {
    fun findAllByQueryGroupId(queryGroupId: Long): List<QueryContent>

    @Transactional
    @Modifying
    @Query("DELETE FROM QueryContent q WHERE q.queryGroup.id = :queryGroupId")
    fun deleteAllByQueryGroupId(@Param("queryGroupId") queryGroupId: Long)

    fun findAllByQueryContentGroupId(contentGroupId: Long): List<QueryContent>

    fun deleteAllByQueryContentGroupId(contentGroupId: Long)
}
