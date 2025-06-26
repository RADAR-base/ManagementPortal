package org.radarbase.management.repository

import org.radarbase.management.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition


@Suppress("unused")
@RepositoryDefinition(domainClass = QueryParticipantContent::class, idClass = Long::class)
interface QueryParticipantContentRepository : JpaRepository<QueryParticipantContent, Long> {

    fun findBySubjectAndQueryGroup(subject: Subject, queryGroup: QueryGroup): List<QueryParticipantContent>

    fun findBySubject(subject: Subject) : List<QueryParticipantContent>

    fun findByQueryContentGroupAndSubject(queryContentGroup: QueryContentGroup, subject: Subject) : List<QueryParticipantContent>
}
