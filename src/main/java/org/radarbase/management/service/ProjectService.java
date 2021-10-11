package org.radarbase.management.service;


import static org.radarbase.management.web.rest.errors.EntityName.PROJECT;

import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.service.mapper.SourceTypeMapper;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
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

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

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
        return projectMapper.projectToProjectDTO(project);
    }

    /**
     * Get all the projects.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<?> findAll(Boolean fetchMinimal, Pageable pageable) {
        Page<Project> projects = projectRepository.findAllWithEagerRelationships(pageable);
        if (!fetchMinimal) {
            return projects.map(projectMapper::projectToProjectDTOReduced);
        } else {
            return projects.map(projectMapper::projectToMinimalProjectDetailsDTO);
        }
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOne(Long id) {
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
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOneByName(String name) {
        log.debug("Request to get Project by name: {}", name);
        return projectRepository.findOneWithEagerRelationshipsByName(name)
                .map(projectMapper::projectToProjectDTO)
                .orElseThrow(() -> new NotFoundException("Project not found with projectName",
                        PROJECT, ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND,
                        Collections.singletonMap("projectName", name)));
    }

    /**
     * Get source-types assigned to a project.
     *
     * @param id the id of the project
     * @return the list of source-types assigned.
     */
    @Transactional(readOnly = true)
    public List<SourceTypeDTO> findSourceTypesByProjectId(Long id) {
        log.debug("Request to get Project.sourceTypes of project: {}", id);
        List<SourceType> sourceTypes = projectRepository.findSourceTypesByProjectId(id);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(sourceTypes);
    }

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        projectRepository.deleteById(id);
    }
}
