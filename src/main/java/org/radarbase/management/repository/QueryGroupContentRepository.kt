package org.radarbase.management.repository

import org.radarbase.management.domain.QueryGroupContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryGroupContent::class, idClass = Long::class)
interface QueryGroupContentRepository: JpaRepository<QueryGroupContent, Long> {
    fun findAllByQueryGroupId(queryGroupId: Long): List<QueryGroupContent>
    fun findAllByQueryGroupIdAndQueryContentIdNotIn(
        queryGroupId: Long,
        contentIds: Set<Long>
    ): List<QueryGroupContent>

    @Transactional
    @Modifying
    @Query("DELETE FROM QueryGroupContent q WHERE q.queryGroup.id = :queryGroupId")
    fun deleteAllByQueryGroupId(@Param("queryGroupId") queryGroupId: Long)

}
