package org.radarbase.management.service

import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.Organization
import org.radarbase.management.domain.Project
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.dto.OrganizationDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.mapper.OrganizationMapper
import org.radarbase.management.service.mapper.ProjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing Organization.
 */
@Service
@Transactional
class OrganizationService(
    @Autowired private val organizationRepository: OrganizationRepository,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val organizationMapper: OrganizationMapper,
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val authService: AuthService,
) {
    /**
     * Save an organization.
     *
     * @param organizationDto the entity to save
     * @return the persisted entity
     */
    fun save(organizationDto: OrganizationDTO): OrganizationDTO {
        log.debug("Request to save Organization : {}", organizationDto)
        var org = organizationMapper.organizationDTOToOrganization(organizationDto)
        org = organizationRepository.save(org)
        return organizationMapper.organizationToOrganizationDTO(org)
    }

    /**
     * Get all the organizations.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(): List<OrganizationDTO> {
        val organizationsOfUser: List<Organization>
        val referents = authService.referentsByScope(Permission.ORGANIZATION_READ)
        organizationsOfUser =
            if (referents.global) {
                organizationRepository.findAll()
            } else {
                val projectNames = referents.allProjects
                val organizationsOfProject =
                    organizationRepository.findAllByProjectNames(projectNames)
                val organizationsOfRole =
                    referents.organizations
                        .mapNotNull { name: String -> organizationRepository.findOneByName(name) }
                (organizationsOfRole + organizationsOfProject)
                    .distinct()
                    .toList()
            }
        return organizationMapper.organizationsToOrganizationDTOs(organizationsOfUser)
    }

    /**
     * Get one organization by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findByName(name: String): OrganizationDTO? {
        log.debug("Request to get Organization by name: {}", name)
        return organizationRepository.findOneByName(name)?.let { organizationMapper.organizationToOrganizationDTO(it) }
    }

    /**
     * Get all projects belonging to the organization.
     *
     * @return the list of projects
     */
    @Transactional(readOnly = true)
    fun findAllProjectsByOrganizationName(organizationName: String): List<ProjectDTO> {
        val referents = authService.referentsByScope(Permission.ORGANIZATION_READ)
        if (referents.isEmpty()) {
            return emptyList()
        }
        val projectStream: List<Project> =
            if (referents.global || referents.hasOrganization(organizationName)) {
                projectRepository.findAllByOrganizationName(organizationName)
            } else if (referents.hasAnyProjects()) {
                projectRepository
                    .findAllByOrganizationName(organizationName)
                    .filter { project: Project ->
                        referents.hasAnyProject(
                            project.projectName!!,
                        )
                    }
            } else {
                return listOf<ProjectDTO>()
            }
        return projectStream
            .mapNotNull { project: Project? -> projectMapper.projectToProjectDTO(project) }
            .toList()
    }

    companion object {
        private val log = LoggerFactory.getLogger(OrganizationService::class.java)
    }
}
