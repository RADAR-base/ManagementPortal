package org.radarbase.management.repository

import org.radarbase.management.domain.QueryContentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryContentGroup::class, idClass = Long::class)
interface QueryContentGroupRepository: JpaRepository<QueryContentGroup, Long> {
    fun findAllByQueryGroupId(queryGroupId: Long): List<QueryContentGroup>
    fun findAllByQueryGroupIdAndQueryContentIdNotIn(
        queryGroupId: Long,
        contentIds: Set<Long>
    ): List<QueryContentGroup>

    @Transactional
    @Modifying
    @Query("DELETE FROM QueryContentGroup q WHERE q.queryGroup.id = :queryGroupId")
    fun deleteAllByQueryGroupId(@Param("queryGroupId") queryGroupId: Long)

}
