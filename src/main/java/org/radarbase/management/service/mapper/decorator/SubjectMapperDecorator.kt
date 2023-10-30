package org.radarbase.management.service.mapper.decorator

import org.mapstruct.MappingTarget
import org.radarbase.management.domain.Group
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.GroupRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.RevisionService
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.dto.SubjectDTO.SubjectStatus
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by nivethika on 30-8-17.
 */
abstract class SubjectMapperDecorator(
    @Autowired @Qualifier("delegate") private val delegate: SubjectMapper,
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val revisionService: RevisionService,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val groupRepository: GroupRepository
) : SubjectMapper {

    override fun subjectToSubjectDTO(subject: Subject?): SubjectDTO? {
        if (subject == null) {
            return null
        }
        val dto = subjectToSubjectWithoutProjectDTO(subject)
        val project = subject.activeProject
            .let { p -> projectRepository.findOneWithEagerRelationships(p?.id!!) }
        dto?.project = projectMapper.projectToProjectDTO(project)
        addAuditInfo(subject, dto)
        return dto
    }

    override fun subjectToSubjectReducedProjectDTO(subject: Subject?): SubjectDTO? {
        if (subject == null) {
            return null
        }
        val dto = subjectToSubjectWithoutProjectDTO(subject)
        subject.activeProject?.let { project -> dto?.project = projectMapper.projectToProjectDTOReduced(project) }
        addAuditInfo(subject, dto)
        return dto
    }

    private fun addAuditInfo(subject: Subject, dto: SubjectDTO?) {
        val auditInfo = revisionService.getAuditInfo(subject)
        dto!!.createdDate = auditInfo.createdAt
        dto.createdBy = auditInfo.createdBy
        dto.lastModifiedDate = auditInfo.lastModifiedAt
        dto.lastModifiedBy = auditInfo.lastModifiedBy
    }

    override fun subjectToSubjectWithoutProjectDTO(subject: Subject?): SubjectDTO? {
        if (subject == null) {
            return null
        }
        val dto = delegate.subjectToSubjectWithoutProjectDTO(subject)
        dto?.status = getSubjectStatus(subject)
        return dto
    }

    override fun subjectDTOToSubject(subjectDto: SubjectDTO?): Subject? {
        if (subjectDto == null) {
            return null
        }
        val subject = delegate.subjectDTOToSubject(subjectDto)
        setSubjectStatus(subjectDto, subject)
        subject?.group = getGroup(subjectDto)
        return subject
    }

    private fun getGroup(subjectDto: SubjectDTO?): Group? {
        return if (subjectDto!!.group == null) {
            null
        } else if (subjectDto.project?.id != null) {
            groupRepository.findByProjectIdAndName(subjectDto.project?.id, subjectDto.group)
                ?: throw BadRequestException(
                        "Group " + subjectDto.group + " not found in project "
                                + subjectDto.project?.id,
                        EntityName.SUBJECT, ErrorConstants.ERR_GROUP_NOT_FOUND)
        } else if (subjectDto.project?.projectName != null) {
        groupRepository.findByProjectNameAndName(subjectDto.project?.projectName, subjectDto.group)
            ?: throw BadRequestException(
                "Group " + subjectDto.group + " not found in project "
                        + subjectDto.project?.projectName,
                EntityName.SUBJECT, ErrorConstants.ERR_GROUP_NOT_FOUND
                )
        } else {
            throw BadRequestException(
                "Group " + subjectDto.group + " cannot be found without a project",
                EntityName.SUBJECT, ErrorConstants.ERR_GROUP_NOT_FOUND
            )
        }
    }

    override fun safeUpdateSubjectFromDTO(subjectDto: SubjectDTO?, @MappingTarget subject: Subject?): Subject? {
        val subjectRetrieved = delegate.safeUpdateSubjectFromDTO(subjectDto, subject)
        setSubjectStatus(subjectDto, subjectRetrieved)
        subject!!.group = getGroup(subjectDto)
        return subjectRetrieved
    }

    private fun getSubjectStatus(subject: Subject): SubjectStatus {
        if (!subject.user!!.activated && !subject.isRemoved!!) {
            return SubjectStatus.DEACTIVATED
        } else if (subject.user!!.activated && !subject.isRemoved!!) {
            return SubjectStatus.ACTIVATED
        } else if (!subject.user!!.activated && subject.isRemoved!!) {
            return SubjectStatus.DISCONTINUED
        }
        return SubjectStatus.INVALID
    }

    private fun setSubjectStatus(subjectDto: SubjectDTO?, subject: Subject?) {
        when (subjectDto!!.status) {
            SubjectStatus.DEACTIVATED -> {
                subject!!.user!!.activated = false
                subject.isRemoved = false
            }

            SubjectStatus.ACTIVATED -> {
                subject!!.user!!.activated = true
                subject.isRemoved = false
            }

            SubjectStatus.DISCONTINUED -> {
                subject!!.user!!.activated = false
                subject.isRemoved = true
            }

            SubjectStatus.INVALID -> {
                subject!!.user!!.activated = true
                subject.isRemoved = true
            }

            else -> {}
        }
    }
}
