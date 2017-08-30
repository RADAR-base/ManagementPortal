package org.radarcns.management.service;

import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.mapper.DeviceTypeMapper;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class ProjectService {

    private final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private DeviceTypeMapper deviceTypeMapper;

    @Autowired
    private UserService userService;

    /**
     * Save a project.
     *
     * @param projectDTO the entity to save
     * @return the persisted entity
     */
    public ProjectDTO save(ProjectDTO projectDTO) {
        log.debug("Request to save Project : {}", projectDTO);
        Project project = projectMapper.projectDTOToProject(projectDTO);
        project = projectRepository.save(project);
        ProjectDTO result = projectMapper.projectToProjectDTO(project);
        return result;
    }

    /**
     *  Get all the projects.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List findAll(Boolean fetchMinimal) {
        List<Project> projects = projectRepository.findAllWithEagerRelationships();
        if(!fetchMinimal){
            return projectMapper.projectsToProjectDTOs(projects);
        } else {
            return projectMapper.projectsToMinimalProjectDetailsDTOs(projects);
        }
    }

    /**
     *  Get one project by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOne(Long id) {
        log.debug("Request to get Project : {}", id);
        Project project = projectRepository.findOneWithEagerRelationships(id);
        ProjectDTO projectDTO = projectMapper.projectToProjectDTO(project);
        return projectDTO;
    }

    /**
     *  Get one project by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeDTO> findDeviceTypesById(Long id) {
        log.debug("Request to get Project.deviceTypes of project: {}", id);
        List<DeviceType> deviceTypes = projectRepository.findDeviceTypesByProjectId(id);
        return deviceTypeMapper.deviceTypesToDeviceTypeDTOs(deviceTypes);
    }

    /**
     *  Delete the  project by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        projectRepository.delete(id);
    }
}
