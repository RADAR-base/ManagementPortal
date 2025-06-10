package org.radarbase.management.repository

import org.radarbase.management.domain.QueryGroupContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryGroupContent::class, idClass = Long::class)
interface QueryGroupContentRepository: JpaRepository<QueryGroupContent, Long> {
}
