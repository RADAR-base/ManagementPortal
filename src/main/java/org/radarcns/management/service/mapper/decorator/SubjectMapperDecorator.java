package org.radarcns.management.service.mapper.decorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.service.dto.AttributeMapDTO;
import org.radarcns.management.service.dto.SubjectDTO;
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
        return dto;
    }

    @Override
    public Subject subjectDTOToSubject(SubjectDTO subjectDTO){

        if ( subjectDTO == null ) {
            return null;
        }

        Subject project = delegate.subjectDTOToSubject(subjectDTO);
        if(subjectDTO.getAttributes()!=null && !subjectDTO.getAttributes().isEmpty()) {
            Map<String, String> attributeMap = new HashMap<>();
            for (AttributeMapDTO attributeMapDTO : subjectDTO.getAttributes()) {
                attributeMap.put(attributeMapDTO.getKey(), attributeMapDTO.getValue());
            }
            project.setAttributes(attributeMap);
        }
        return project;
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

}
