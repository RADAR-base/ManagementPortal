package org.radarcns.management.service.mapper.decorator;

import static org.radarcns.management.service.dto.ProjectDTO.HUMAN_READABLE_PROJECT_NAME;

import org.radarcns.management.domain.Project;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.dto.MinimalProjectDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nivethika on 30-8-17.
 */
public abstract class ProjectMapperDecorator implements ProjectMapper {

    @Autowired
    @Qualifier("delegate")
    private ProjectMapper delegate;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public ProjectDTO projectToProjectDTO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO dto = delegate.projectToProjectDTO(project);
        dto.setHumanReadableProjectName(project.getAttributes().get(HUMAN_READABLE_PROJECT_NAME));
        return dto;
    }


    @Override
    public ProjectDTO projectToProjectDTOReduced(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO dto = delegate.projectToProjectDTOReduced(project);
        dto.setHumanReadableProjectName(project.getAttributes().get(HUMAN_READABLE_PROJECT_NAME));
        dto.setSourceTypes(null);
        return dto;
    }

    @Override
    public Project projectDTOToProject(ProjectDTO projectDto) {
        if (projectDto == null) {
            return null;
        }

        Project project = delegate.projectDTOToProject(projectDto);
        String projectName = projectDto.getHumanReadableProjectName();
        if (projectName != null && !projectName.isEmpty()) {
            project.getAttributes().put(HUMAN_READABLE_PROJECT_NAME, projectName);
        }
        return project;
    }

    @Override
    public Project descriptiveDTOToProject(MinimalProjectDetailsDTO minimalProjectDetailsDto) {
        if (minimalProjectDetailsDto == null) {
            return null;
        }
        return projectRepository.getOne(minimalProjectDetailsDto.getId());
    }
}

