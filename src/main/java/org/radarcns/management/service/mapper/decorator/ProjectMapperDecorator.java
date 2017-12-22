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
        Set<AttributeMapDTO> attributeMapDTOList = new HashSet<>();
        if (project.getAttributes() != null) {
            for (Entry<String, String> entry : project.getAttributes().entrySet()) {
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
    public Project projectDTOToProject(ProjectDTO projectDTO) {

        if (projectDTO == null) {
            return null;
        }

        Project project = delegate.projectDTOToProject(projectDTO);
        if (projectDTO.getAttributes() != null && !projectDTO.getAttributes().isEmpty()) {
            Map<String, String> attributeMap = new HashMap<>();
            for (AttributeMapDTO attributeMapDTO : projectDTO.getAttributes()) {
                attributeMap.put(attributeMapDTO.getKey(), attributeMapDTO.getValue());
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
    public List<Project> projectDTOsToProjects(List<ProjectDTO> projectDTOs) {

        if (projectDTOs == null) {
            return null;
        }

        List<Project> list = new ArrayList<Project>();
        for (ProjectDTO projectDTO : projectDTOs) {
            list.add(this.projectDTOToProject(projectDTO));
        }

        return list;
    }
}

