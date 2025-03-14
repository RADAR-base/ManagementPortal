package org.radarbase.management.repository

import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.QueryLogic
import org.radarbase.management.domain.SourceData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryGroup::class, idClass = Long::class)
interface QueryGroupRepository : JpaRepository<QueryGroup, Long>  {



}
