package org.radarbase.management.repository

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryEvaluation
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryContent::class, idClass = Long::class)
interface QueryContentRepository : JpaRepository<QueryContent, Long> {
    fun findAllByQueryGroupId(queryGroupId: Long): List<QueryContent>
}
