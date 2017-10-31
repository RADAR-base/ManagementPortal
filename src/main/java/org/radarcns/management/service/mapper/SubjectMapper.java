package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.decorator.SubjectMapperDecorator;

/**
 * Mapper for the entity Subject and its DTO SubjectDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class, SourceMapper.class})
@DecoratedWith(SubjectMapperDecorator.class)
public interface SubjectMapper {

    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "user.createdBy", target = "createdBy")
    @Mapping(source = "user.createdDate", target = "createdDate")
    @Mapping(source = "user.lastModifiedBy", target = "lastModifiedBy")
    @Mapping(source = "user.lastModifiedDate", target = "lastModifiedDate")
    @Mapping(target = "attributes" , ignore = true)
    @Mapping(target = "status" , ignore = true)
    @Mapping(target = "project" , ignore = true)
    SubjectDTO subjectToSubjectDTO(Subject subject);

    List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects);

    @Mapping(source = "login", target = "user.login")
    @Mapping(source = "createdBy", target = "user.createdBy")
    @Mapping(source = "createdDate", target = "user.createdDate")
    @Mapping(source = "lastModifiedBy", target = "user.lastModifiedBy")
    @Mapping(source = "lastModifiedDate", target = "user.lastModifiedDate")
    @Mapping(target = "user.email" , ignore = true)
    @Mapping(target = "attributes" , ignore = true)
    @Mapping(target = "user.activated" , ignore = true)
    @Mapping(target = "removed" , ignore = true)
    Subject subjectDTOToSubject(SubjectDTO subjectDTO);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "attributes" , ignore = true)
    @Mapping(target = "removed" , ignore = true)
    Subject safeUpdateSubjectFromDTO(SubjectDTO subjectDTO, @MappingTarget Subject subject);

    List<Subject> subjectDTOsToSubjects(List<SubjectDTO> subjectDTOS);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default Subject subjectFromId(Long id) {
        if (id == null) {
            return null;
        }
        Subject subject = new Subject();
        subject.setId(id);
        return subject;
    }


}
