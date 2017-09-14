package org.radarcns.management.service.mapper.decorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.mapstruct.MappingTarget;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.service.dto.AttributeMapDTO;
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
    public SubjectDTO subjectToSubjectDTO(Subject subject){
        if ( subject == null ) {
            return null;
        }
        SubjectDTO dto = delegate.subjectToSubjectDTO(subject);
        Set<AttributeMapDTO> attributeMapDTOList = new HashSet<>();
        if(subject.getAttributes()!=null) {
            for (Entry<String, String> entry : subject.getAttributes().entrySet()) {
                AttributeMapDTO attributeMapDTO = new AttributeMapDTO();
                attributeMapDTO.setKey(entry.getKey());
                attributeMapDTO.setValue(entry.getValue());
                attributeMapDTOList.add(attributeMapDTO);
            }
            dto.setAttributes(attributeMapDTOList);
        }
        dto.setStatus(getSubjectStatus(subject));
        for (Role role : subject.getUser().getRoles()) {
            dto.setProject(projectMapper.projectToProjectDTO(role.getProject()));
        }
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
        if(subjectDTO.getAttributes()!=null && !subjectDTO.getAttributes().isEmpty()) {
            Map<String, String> attributeMap = new HashMap<>();
            for (AttributeMapDTO attributeMapDTO : subjectDTO.getAttributes()) {
                attributeMap.put(attributeMapDTO.getKey(), attributeMapDTO.getValue());
            }
            subject.setAttributes(attributeMap);
        }
    }

    private SubjectStatus getSubjectStatus(Subject subject) {
        if(!subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.DEACTIVATED;
        }
        else if( subject.getUser().getActivated() && !subject.isRemoved()) {
            return SubjectStatus.ACTIVATED;
        }
        else if(subject.getUser().getActivated() && subject.isRemoved()) {
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
                subject.getUser().setActivated(true);
                subject.setRemoved(true);
                break;

        }
        return subject;
    }

}
