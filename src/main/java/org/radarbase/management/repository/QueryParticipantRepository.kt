package org.radarbase.management.repository

import org.radarbase.management.domain.QueryParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryParticipant::class, idClass = Long::class)
interface QueryParticipantRepository : JpaRepository<QueryParticipant, Long>  {



}
