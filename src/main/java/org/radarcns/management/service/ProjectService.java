package org.radarcns.management.service;


import static org.radarcns.management.web.rest.errors.EntityName.PROJECT;

import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

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
     * @param projectDto the entity to save
     * @return the persisted entity
     */
    public ProjectDTO save(ProjectDTO projectDto) {
        log.debug("Request to save Project : {}", projectDto);
        Project project = projectMapper.projectDTOToProject(projectDto);
        project = projectRepository.save(project);
        ProjectDTO result = projectMapper.projectToProjectDTO(project);
        return result;
    }

    /**
     * Get all the projects.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page findAll(Boolean fetchMinimal, Pageable pageable) {
        Page<Project> projects = projectRepository.findAllWithEagerRelationships(pageable);
        if (!fetchMinimal) {
            return projects.map(projectMapper::projectToProjectDTO);
        } else {
            return projects.map(projectMapper::projectToMinimalProjectDetailsDTO);
        }
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity
     * @return the entity
     * @throws NotFoundException if there is no project with the given id
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOne(Long id) throws NotFoundException {
        log.debug("Request to get Project : {}", id);
        return projectRepository.findOneWithEagerRelationships(id)
                .map(projectMapper::projectToProjectDTO)
                .orElseThrow(() -> new NotFoundException("Project not found with id", PROJECT,
                        ErrorConstants.ERR_PROJECT_ID_NOT_FOUND,
                        Collections.singletonMap("id", id.toString())));
    }

    /**
     * Get one project by name.
     *
     * @param name the name of the entity
     * @return the entity
     * @throws NotFoundException if there is no project with the given name
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOneByName(String name) throws NotFoundException {
        log.debug("Request to get Project by name: {}", name);
        return projectRepository.findOneWithEagerRelationshipsByName(name)
                .map(projectMapper::projectToProjectDTO)
                .orElseThrow(() -> new NotFoundException("Project not found with projectName",
                        PROJECT, ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND,
                        Collections.singletonMap("projectName", name)));
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public List<SourceTypeDTO> findSourceTypesById(Long id) {
        log.debug("Request to get Project.sourceTypes of project: {}", id);
        List<SourceType> sourceTypes = projectRepository.findSourceTypesByProjectId(id);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(sourceTypes);
    }

    /**
     * Delete the  project by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        projectRepository.delete(id);
    }
}
