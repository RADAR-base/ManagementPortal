package org.radarcns.management.service.mapper.decorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.radarcns.management.domain.Project;
import org.radarcns.management.service.dto.AttributeMapDTO;
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
        Set<AttributeMapDTO> attributeMapDtoList = new HashSet<>();
        if (project.getAttributes() != null) {
            for (Entry<String, String> entry : project.getAttributes().entrySet()) {
                AttributeMapDTO attributeMapDto = new AttributeMapDTO();
                attributeMapDto.setKey(entry.getKey());
                attributeMapDto.setValue(entry.getValue());
                attributeMapDtoList.add(attributeMapDto);
            }
            dto.setAttributes(attributeMapDtoList);
        }
        return dto;
    }

    @Override
    public Project projectDTOToProject(ProjectDTO projectDto) {

        if (projectDto == null) {
            return null;
        }

        Project project = delegate.projectDTOToProject(projectDto);
        if (projectDto.getAttributes() != null && !projectDto.getAttributes().isEmpty()) {
            Map<String, String> attributeMap = new HashMap<>();
            for (AttributeMapDTO attributeMapDto : projectDto.getAttributes()) {
                attributeMap.put(attributeMapDto.getKey(), attributeMapDto.getValue());
            }
            project.setAttributes(attributeMap);
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

