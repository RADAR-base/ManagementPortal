package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "attributes", ignore = true)
    ProjectDTO projectToProjectDTO(Project project);

    List<ProjectDTO> projectsToProjectDTOs(List<Project> projects);

    @Mapping(target = "attributes", ignore = true)
    Project projectDTOToProject(ProjectDTO projectDTO);

    List<Project> projectDTOsToProjects(List<ProjectDTO> projectDTOs);

    MinimalProjectDetailsDTO projectToMinimalProjectDetailsDTO(Project project);

    List<MinimalProjectDetailsDTO> projectsToMinimalProjectDetailsDTOs(List<Project> projects);

    Project descriptiveDTOToProject(MinimalProjectDetailsDTO minimalProjectDetailsDTO);

    List<Project> descriptiveDTOsToProjects(
            List<MinimalProjectDetailsDTO> minimalProjectDetailsDTOS);

    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity
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
