package org.radarbase.management.service.mapper.decorator;

import static org.radarbase.management.service.dto.ProjectDTO.HUMAN_READABLE_PROJECT_NAME;

import org.radarbase.management.domain.Project;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.service.OAuthClientService;
import org.radarbase.management.service.dto.MinimalProjectDetailsDTO;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.web.rest.errors.BadRequestException;
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

    @Autowired
    private OAuthClientService oAuthClientService;

    @Override
    public ProjectDTO projectToProjectDTO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO dto = delegate.projectToProjectDTO(project);

        dto.setHumanReadableProjectName(project.getAttributes().get(HUMAN_READABLE_PROJECT_NAME));

        try {
            dto.setPersistentTokenTimeout(
                    oAuthClientService.getMetaTokenTimeout(true, project).toMillis());
        } catch (BadRequestException ex) {
            dto.setPersistentTokenTimeout(null);
        }

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

