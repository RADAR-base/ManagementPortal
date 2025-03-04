package org.radarbase.management.repository

import org.radarbase.management.domain.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition


@Suppress("unused")
@RepositoryDefinition(domainClass = Query::class, idClass = Long::class)
interface QueryRepository : JpaRepository<Query, Long> {
}
