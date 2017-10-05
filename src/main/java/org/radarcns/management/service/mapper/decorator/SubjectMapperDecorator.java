package org.radarcns.management.service.mapper.decorator;

import org.mapstruct.MappingTarget;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.AttributeMapDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.dto.SubjectDTO.SubjectStatus;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
    public SubjectDTO subjectToSubjectDTO(Subject subject){
        if ( subject == null ) {
            return null;
        }
        SubjectDTO dto = delegate.subjectToSubjectDTO(subject);
        dto.setAttributes(subject.getAttributes());
        dto.setStatus(getSubjectStatus(subject));
        Optional<Role> role = subject.getUser().getRoles().stream()
                .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PARTICIPANT))
                .findFirst();

        role.ifPresent(role1 -> dto.setProject(projectMapper.projectToProjectDTO(role1.getProject())));

        return dto;
    }

    @Override
    public Subject subjectDTOToSubject(SubjectDTO subjectDTO){

        if ( subjectDTO == null ) {
            return null;
        }

        Subject subject = delegate.subjectDTOToSubject(subjectDTO);
        extractAttributeData(subjectDTO, subject);
        setSubjectStatus(subjectDTO,subject);
        return subject;
    }

    @Override
    public List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects){
        if ( subjects == null ) {
            return null;
        }

        List<SubjectDTO> list = new ArrayList<>();
        for ( Subject subject: subjects ) {
            list.add( this.subjectToSubjectDTO( subject) );
        }

        return list;
    }

    @Override
    public List<Subject> subjectDTOsToSubjects(List<SubjectDTO> subjectDTOS){

        if ( subjectDTOS == null ) {
            return null;
        }

        List<Subject> list = new ArrayList<>();
        for ( SubjectDTO subjectDTO: subjectDTOS ) {
            list.add(this.subjectDTOToSubject( subjectDTO ) );
        }

        return list;
    }

    @Override
    public Subject safeUpdateSubjectFromDTO(SubjectDTO subjectDTO, @MappingTarget Subject subject) {
        Subject subjectRetrieved = delegate.safeUpdateSubjectFromDTO(subjectDTO,subject);
        extractAttributeData(subjectDTO , subjectRetrieved);
        setSubjectStatus(subjectDTO,subjectRetrieved);
        return subjectRetrieved;
    }


    private void extractAttributeData(SubjectDTO subjectDTO, Subject subject) {
        subject.setAttributes(subjectDTO.getAttributes());
    }

    private SubjectStatus getSubjectStatus(Subject subject) {
        if(!subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.DEACTIVATED;
        }
        else if( subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.ACTIVATED;
        }
        else if(subject.isRemoved()) {
            return SubjectStatus.DISCONTINUED;
        }
        return SubjectStatus.DEACTIVATED;
    }

    private Subject setSubjectStatus (SubjectDTO subjectDTO, Subject subject) {
        switch (subjectDTO.getStatus()) {
            case DEACTIVATED:
                subject.getUser().setActivated(false);
                subject.setRemoved(false);
                break;
            case ACTIVATED:
                subject.getUser().setActivated(true);
                subject.setRemoved(false);
                break;
            case DISCONTINUED:
                subject.setRemoved(true);
                break;

        }
        return subject;
    }

}
