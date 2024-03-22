package org.radarbase.management.repository

import org.radarbase.management.domain.SourceData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository

/**
 * Spring Data JPA repository for the SourceData entity.
 */
@Suppress("unused")
@RepositoryDefinition(domainClass = SourceData::class, idClass = Long::class)
interface SourceDataRepository : JpaRepository<SourceData, Long>, RevisionRepository<SourceData, Long, Int> {
    fun findOneBySourceDataName(sourceDataName: String?): SourceData?
}
