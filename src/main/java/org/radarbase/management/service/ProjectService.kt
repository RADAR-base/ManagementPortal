package org.radarbase.management.service

import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.Project
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.service.mapper.SourceTypeMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
open class ProjectService(
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val sourceTypeMapper: SourceTypeMapper,
    @Autowired private val authService: AuthService
) {

    /**
     * Save a project.
     *
     * @param projectDto the entity to save
     * @return the persisted entity
     */
    fun save(projectDto: ProjectDTO): ProjectDTO {
        log.debug("Request to save Project : {}", projectDto)
        var project = projectMapper.projectDTOToProject(projectDto)
        project = project?.let { projectRepository.save(it) }
        return projectMapper.projectToProjectDTO(project)!!
    }

    /**
     * Get all the projects.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    open fun findAll(fetchMinimal: Boolean, pageable: Pageable): Page<*> {
        val projects: Page<Project>
        val referents = authService.referentsByScope(Permission.PROJECT_READ)
        projects = if (referents.isEmpty()) {
            PageImpl(listOf<Project>())
        } else if (referents.global) {
            projectRepository.findAllWithEagerRelationships(pageable)
        } else {
            projectRepository.findAllWithEagerRelationshipsInOrganizationsOrProjects(
                pageable, referents.organizations, referents.allProjects
            )
        }
        return if (!fetchMinimal) {
            projects.map { project: Project -> projectMapper.projectToProjectDTO(project) }
        } else {
            projects.map { project: Project -> projectMapper.projectToMinimalProjectDetailsDTO(project) }
        }
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    open fun findOne(id: Long): ProjectDTO? {
        log.debug("Request to get Project : {}", id)
        val project = projectRepository.findOneWithEagerRelationships(id)
            ?: throw NotFoundException(
                "Project not found with id",
                EntityName.PROJECT,
                ErrorConstants.ERR_PROJECT_ID_NOT_FOUND,
                Collections.singletonMap("id", id.toString())
            )

        return projectMapper.projectToProjectDTO(project)
    }

    /**
     * Get one project by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    open fun findOneByName(name: String): ProjectDTO {
        log.debug("Request to get Project by name: {}", name)
        val project = projectRepository.findOneWithEagerRelationshipsByName(name)
            ?: throw NotFoundException(
                "Project not found with projectName $name",
                EntityName.PROJECT,
                ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND,
                Collections.singletonMap("projectName", name))

        return projectMapper.projectToProjectDTO(project)!!
    }

    /**
     * Get source-types assigned to a project.
     *
     * @param id the id of the project
     * @return the list of source-types assigned.
     */
    @Transactional(readOnly = true)
    open fun findSourceTypesByProjectId(id: Long): List<SourceTypeDTO> {
        log.debug("Request to get Project.sourceTypes of project: {}", id)
        val sourceTypes = projectRepository.findSourceTypesByProjectId(id)
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(sourceTypes).filterNotNull()
    }

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity
     */
    fun delete(id: Long) {
        log.debug("Request to delete Project : {}", id)
        projectRepository.deleteById(id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProjectService::class.java)
    }
}
