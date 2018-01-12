package org.radarcns.management.service.mapper.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.mapstruct.MappingTarget;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Subject;
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

    @Override
    public SubjectDTO subjectToSubjectDTO(Subject subject) {
        if (subject == null) {
            return null;
        }
        SubjectDTO dto = delegate.subjectToSubjectDTO(subject);
        dto.setAttributes(subject.getAttributes());
        dto.setStatus(getSubjectStatus(subject));
        Optional<Role> role = subject.getUser().getRoles().stream()
                .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PARTICIPANT))
                .findFirst();

        role.ifPresent(
                role1 -> dto.setProject(projectMapper.projectToProjectDTO(role1.getProject())));

        return dto;
    }

    @Override
    public Subject subjectDTOToSubject(SubjectDTO subjectDto) {

        if (subjectDto == null) {
            return null;
        }

        Subject subject = delegate.subjectDTOToSubject(subjectDto);
        extractAttributeData(subjectDto, subject);
        setSubjectStatus(subjectDto, subject);
        return subject;
    }

    @Override
    public List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects) {
        if (subjects == null) {
            return null;
        }

        List<SubjectDTO> list = new ArrayList<>();
        for (Subject subject : subjects) {
            list.add(this.subjectToSubjectDTO(subject));
        }

        return list;
    }

    @Override
    public List<Subject> subjectDTOsToSubjects(List<SubjectDTO> subjectDtos) {

        if (subjectDtos == null) {
            return null;
        }

        List<Subject> list = new ArrayList<>();
        for (SubjectDTO subjectDto : subjectDtos) {
            list.add(this.subjectDTOToSubject(subjectDto));
        }

        return list;
    }

    @Override
    public Subject safeUpdateSubjectFromDTO(SubjectDTO subjectDto, @MappingTarget Subject subject) {
        Subject subjectRetrieved = delegate.safeUpdateSubjectFromDTO(subjectDto, subject);
        extractAttributeData(subjectDto, subjectRetrieved);
        setSubjectStatus(subjectDto, subjectRetrieved);
        return subjectRetrieved;
    }


    private void extractAttributeData(SubjectDTO subjectDto, Subject subject) {
        subject.setAttributes(subjectDto.getAttributes());
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

    private Subject setSubjectStatus(SubjectDTO subjectDto, Subject subject) {
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
        return subject;
    }

}
