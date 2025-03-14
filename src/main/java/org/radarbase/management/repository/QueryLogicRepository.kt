package org.radarbase.management.repository

import org.radarbase.management.domain.QueryLogic
import org.radarbase.management.domain.SourceData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryLogic::class, idClass = Long::class)
interface QueryLogicRepository : JpaRepository<QueryLogic, Long>{
    fun findByQueryGroupId(queryGroupId: Long): List<QueryLogic>

}
