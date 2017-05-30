package org.radarcns.management.service.mapper;

import java.util.Set;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.Project;
import org.radarcns.management.service.dto.ProjectDTO;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class, DeviceTypeMapper.class})
public interface ProjectMapper {

    Project projectDTOToProject( ProjectDTO projectDTO);

    ProjectDTO projectToProjectDto (Project project);

    Set<Project> projectDtosToProjects (Set<ProjectDTO> projectDTOS);

    Set<ProjectDTO> projectsToProjectDtos( Set<Project> projects);



}
