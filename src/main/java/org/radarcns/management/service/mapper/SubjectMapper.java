package org.radarcns.management.service.mapper;

import java.util.List;
import javax.ws.rs.core.Context;
import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.decorator.SubjectMapperDecorator;

/**
 * Mapper for the entity Subject and its DTO SubjectDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class,
        SourceMapper.class, RoleMapper.class})
@DecoratedWith(SubjectMapperDecorator.class)
public interface SubjectMapper {

    @Mapping(source = "user.login", target = "login")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    SubjectDTO subjectToSubjectDTO(Subject subject);

    @Mapping(source = "user.login", target = "login")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    SubjectDTO subjectToSubjectReducedProjectDTO(Subject subject);

    @Named(value = "subjectReducedProjectDTO")
    @Mapping(source = "user.login", target = "login")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    SubjectDTO subjectToSubjectWithoutProjectDTO(Subject subject);

    @IterableMapping(qualifiedByName = "subjectReducedProjectDTO")
    List<SubjectDTO> subjectsToSubjectReducedProjectDTOs(List<Subject> subjects);

    @Mapping(source = "login", target = "user.login")
    @Mapping(target = "user.email", ignore = true)
    @Mapping(target = "user.activated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(source = "roles" , target = "user.roles")
    @Mapping(target = "metaTokens", ignore = true)
    Subject subjectDTOToSubject(SubjectDTO subjectDto);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "metaTokens", ignore = true)
    Subject safeUpdateSubjectFromDTO(SubjectDTO subjectDto, @MappingTarget Subject subject);

    /**
     * Generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity.
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
