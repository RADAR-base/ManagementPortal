package org.radarcns.management.service;

import java.util.List;
import java.util.stream.Collectors;
import org.radarcns.management.config.Constants;
import org.radarcns.management.domain.Project;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika on 24-5-17.
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAllWithEagerRelationships().stream()
            .map(projectMapper::projectToProjectDto).collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO projectDTO) {

        return projectMapper.projectToProjectDto(
            projectRepository.save(projectMapper.projectDTOToProject(projectDTO)));
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO) {
        if (projectDTO.getId() == null) {
            return createProject(projectDTO);
        }
        return projectMapper.projectToProjectDto(
            projectRepository.save(projectMapper.projectDTOToProject(projectDTO)));
    }

    @Transactional(readOnly = true)
    public ProjectDTO findProjectById(Long id) {
        return projectMapper.projectToProjectDto(projectRepository.findOneWithEagerRelationships(id));

    }

    public void deleteProject(Long id) {
        projectRepository.delete(id);
    }

}
