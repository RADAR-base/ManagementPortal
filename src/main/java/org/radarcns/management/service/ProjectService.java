package org.radarcns.management.service;


import java.util.List;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private SourceTypeMapper sourceTypeMapper;


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
    public Page findAll(Boolean fetchMinimal , Pageable pageable) {
        Page<Project> projects = projectRepository.findAllWithEagerRelationships(pageable);
        if(!fetchMinimal){
            return projects.map(projectMapper::projectToProjectDTO);
        } else {
            return projects.map(projectMapper::projectToMinimalProjectDetailsDTO);
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
     *  Get one project by name.
     *
     *  @param name the name of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOneByName(String name) {
        log.debug("Request to get Project by name: {}", name);
        Project project = projectRepository.findOneWithEagerRelationshipsByName(name);
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
    public List<SourceTypeDTO> findSourceTypesById(Long id) {
        log.debug("Request to get Project.sourceTypes of project: {}", id);
        List<SourceType> sourceTypes = projectRepository.findSourceTypesByProjectId(id);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(sourceTypes);
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
