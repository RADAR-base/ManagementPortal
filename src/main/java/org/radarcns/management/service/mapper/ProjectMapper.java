package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.radarcns.management.domain.Project;
import org.radarcns.management.service.dto.MinimalProjectDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.mapper.decorator.ProjectMapperDecorator;

/**
 * Mapper for the entity Project and its DTO ProjectDTO.
 */
@Mapper(componentModel = "spring", uses = {SourceTypeMapper.class,})
@DecoratedWith(ProjectMapperDecorator.class)
public interface ProjectMapper {

    @Mapping(target = "humanReadableProjectName", ignore = true)
    @Mapping(target = "persistentTokenTimeout", ignore = true)
    ProjectDTO projectToProjectDTO(Project project);

    @Named(value = "projectReducedDTO")
    @Mapping(target = "humanReadableProjectName", ignore = true)
    @Mapping(target = "sourceTypes", ignore = true)
    @Mapping(target = "persistentTokenTimeout", ignore = true)
    ProjectDTO projectToProjectDTOReduced(Project project);

    @IterableMapping(qualifiedByName = "projectReducedDTO")
    List<ProjectDTO> projectsToProjectDTOs(List<Project> projects);

    @Mapping(target = "roles", ignore = true)
    Project projectDTOToProject(ProjectDTO projectDto);

    List<Project> projectDTOsToProjects(List<ProjectDTO> projectDtos);

    MinimalProjectDetailsDTO projectToMinimalProjectDetailsDTO(Project project);

    List<MinimalProjectDetailsDTO> projectsToMinimalProjectDetailsDTOs(List<Project> projects);

    @Mapping(target = "description", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "projectStatus", ignore = true)
    @Mapping(target = "sourceTypes", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    Project descriptiveDTOToProject(MinimalProjectDetailsDTO minimalProjectDetailsDto);

    List<Project> descriptiveDTOsToProjects(
            List<MinimalProjectDetailsDTO> minimalProjectDetailsDtos);

    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity.
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default Project projectFromId(Long id) {
        if (id == null) {
            return null;
        }
        Project project = new Project();
        project.setId(id);
        return project;
    }


}
