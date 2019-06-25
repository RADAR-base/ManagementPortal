package org.radarcns.management.service.mapper.decorator;

import org.mapstruct.MappingTarget;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.audit.EntityAuditInfo;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.RevisionService;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.dto.SubjectDTO.SubjectStatus;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nivethika on 30-8-17.
 */
public abstract class SubjectMapperDecorator implements SubjectMapper {

    @Autowired
    @Qualifier("delegate")
    private SubjectMapper delegate;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public SubjectDTO subjectToSubjectDTO(Subject subject) {
        SubjectDTO dto = subjectToSubjectWithoutProjectDTO(subject);
        if (dto == null) {
            return null;
        }
        Project project = subject.getActiveProject()
                .flatMap(p ->  projectRepository.findOneWithEagerRelationships(p.getId()))
                .orElse(null);
        dto.setProject(projectMapper.projectToProjectDTO(project));

        addAuditInfo(subject, dto);

        return dto;
    }

    @Override
    public SubjectDTO subjectToSubjectReducedProjectDTO(Subject subject) {
        SubjectDTO dto = subjectToSubjectWithoutProjectDTO(subject);
        if (dto == null) {
            return null;
        }

        subject.getActiveProject()
                .ifPresent(project -> dto.setProject(
                        projectMapper.projectToProjectDTOReduced(project)));

        addAuditInfo(subject, dto);

        return dto;
    }

    private void addAuditInfo(Subject subject, SubjectDTO dto) {
        EntityAuditInfo auditInfo = revisionService.getAuditInfo(subject);
        dto.setCreatedDate(auditInfo.getCreatedAt());
        dto.setCreatedBy(auditInfo.getCreatedBy());
        dto.setLastModifiedDate(auditInfo.getLastModifiedAt());
        dto.setLastModifiedBy(auditInfo.getLastModifiedBy());
    }

    @Override
    public SubjectDTO subjectToSubjectWithoutProjectDTO(Subject subject) {
        if (subject == null) {
            return null;
        }
        SubjectDTO dto = delegate.subjectToSubjectWithoutProjectDTO(subject);
        dto.setStatus(getSubjectStatus(subject));

        return dto;
    }

    @Override
    public Subject subjectDTOToSubject(SubjectDTO subjectDto) {
        if (subjectDto == null) {
            return null;
        }

        Subject subject = delegate.subjectDTOToSubject(subjectDto);
        setSubjectStatus(subjectDto, subject);
        return subject;
    }

    @Override
    public Subject safeUpdateSubjectFromDTO(SubjectDTO subjectDto, @MappingTarget Subject subject) {
        Subject subjectRetrieved = delegate.safeUpdateSubjectFromDTO(subjectDto, subject);
        setSubjectStatus(subjectDto, subjectRetrieved);
        return subjectRetrieved;
    }

    private SubjectStatus getSubjectStatus(Subject subject) {
        if (!subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.DEACTIVATED;
        } else if (subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.ACTIVATED;
        } else if (!subject.getUser().getActivated() && subject.isRemoved()) {
            return SubjectStatus.DISCONTINUED;
        }
        return SubjectStatus.INVALID;
    }

    private void setSubjectStatus(SubjectDTO subjectDto, Subject subject) {
        switch (subjectDto.getStatus()) {
            case DEACTIVATED:
                subject.getUser().setActivated(false);
                subject.setRemoved(false);
                break;
            case ACTIVATED:
                subject.getUser().setActivated(true);
                subject.setRemoved(false);
                break;
            case DISCONTINUED:
                subject.getUser().setActivated(false);
                subject.setRemoved(true);
                break;
            case INVALID:
                subject.getUser().setActivated(true);
                subject.setRemoved(true);
                break;
            default:
                break;
        }
    }

}
