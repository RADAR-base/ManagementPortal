package org.radarcns.management.service.mapper.decorator;

import static org.radarcns.management.service.dto.ProjectDTO.HUMAN_READABLE_PROJECT_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.radarcns.management.domain.Project;
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

    @Override
    public ProjectDTO projectToProjectDTO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO dto = delegate.projectToProjectDTO(project);
        List<String> humanReadablename = project.getAttributes().entrySet().stream()
            .filter( (s)-> HUMAN_READABLE_PROJECT_NAME.equals(s.getKey()))
            .map(Entry::getValue)
            .collect(Collectors.toList());
        if(!humanReadablename.isEmpty()) {
            dto.setHumanReadableProjectName(humanReadablename.get(0));
        }
        return dto;
    }

    @Override
    public Project projectDTOToProject(ProjectDTO projectDto) {

        if (projectDto == null) {
            return null;
        }

        Project project = delegate.projectDTOToProject(projectDto);
        if (projectDto.getHumanReadableProjectName() != null && !projectDto
            .getHumanReadableProjectName().isEmpty()) {
            project.getAttributes().put(HUMAN_READABLE_PROJECT_NAME, projectDto
                .getHumanReadableProjectName());
        }
        return project;
    }

    @Override
    public List<ProjectDTO> projectsToProjectDTOs(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        List<ProjectDTO> list = new ArrayList<ProjectDTO>();
        for (Project project : projects) {
            list.add(this.projectToProjectDTO(project));
        }

        return list;
    }

    @Override
    public List<Project> projectDTOsToProjects(List<ProjectDTO> projectDtos) {

        if (projectDtos == null) {
            return null;
        }

        List<Project> list = new ArrayList<Project>();
        for (ProjectDTO projectDto : projectDtos) {
            list.add(this.projectDTOToProject(projectDto));
        }

        return list;
    }
}

