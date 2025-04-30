package org.radarbase.management.service

import org.radarbase.management.domain.Project
import org.radarbase.management.domain.SourceType
import org.radarbase.management.domain.Subject
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.springframework.data.domain.Page
import java.net.URL

interface SubjectService {

    suspend fun createSubject(
        subjectDto: SubjectDTO,
        activated: Boolean? = true,
    ): SubjectDTO?

    suspend fun createSubject(
        id: String,
        projectDto: ProjectDTO,
        externalId: String,
        attributes: Map<String, String> = HashMap()
    ): SubjectDTO?

    suspend fun updateSubject(newSubjectDto: SubjectDTO): SubjectDTO?

    fun activateSubject(login: String): SubjectDTO?

    fun discontinueSubject(subjectDto: SubjectDTO): SubjectDTO?

    suspend fun assignOrUpdateSource(
        subject: Subject,
        sourceType: SourceType,
        project: Project?,
        sourceRegistrationDto: MinimalSourceDetailsDTO,
    ): MinimalSourceDetailsDTO

    fun getSources(subject: Subject): List<MinimalSourceDetailsDTO>

    fun deleteSubject(login: String?)

    fun findSubjectSourcesFromRevisions(subject: Subject): List<MinimalSourceDetailsDTO>?

    fun findRevision(
        login: String?,
        revision: Int?,
    ): SubjectDTO

    fun getLatestRevision(login: String?): SubjectDTO

    fun findOneByLogin(login: String?): Subject

    fun findAll(criteria: SubjectCriteria): Page<Subject>
    fun getPrivacyPolicyUrl(subject: Subject): URL


}