package org.radarbase.management.repository

import org.radarbase.management.domain.QueryEvaluation
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.QueryLogic
import org.radarbase.management.domain.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition

@Suppress("unused")
@RepositoryDefinition(domainClass = QueryEvaluation::class, idClass = Long::class)
interface QueryEvaluationRepository : JpaRepository<QueryEvaluation, Long>  {


    fun findBySubjectAndQueryGroup(subject: Subject, queryGroup: QueryGroup): List<QueryEvaluation>

    fun findBySubject(subject: Subject): List<QueryEvaluation>
}
