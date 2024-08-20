package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.domain.Project
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.MetaTokenService
import org.radarbase.management.service.dto.MinimalProjectDetailsDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

/**
 * Created by nivethika on 30-8-17.
 */
abstract class ProjectMapperDecorator : ProjectMapper {

    @Autowired @Qualifier("delegate") private lateinit var delegate: ProjectMapper
    @Autowired private lateinit var organizationRepository: OrganizationRepository
    @Autowired private lateinit var projectRepository: ProjectRepository
    //@Autowired private lateinit var metaTokenService: MetaTokenService

    override fun projectToProjectDTO(project: Project?): ProjectDTO? {
        val dto = delegate.projectToProjectDTO(project)
        dto?.humanReadableProjectName = project?.attributes?.get(ProjectDTO.HUMAN_READABLE_PROJECT_NAME)
//        try {
//            dto?.persistentTokenTimeout = metaTokenService.getMetaTokenTimeout(true, project).toMillis()
//        } catch (ex: BadRequestException) {
//            dto?.persistentTokenTimeout = null
//        }
        return dto
    }

    override fun projectToProjectDTOReduced(project: Project?): ProjectDTO? {
        if (project == null) {
            return null
        }
        val dto = delegate.projectToProjectDTOReduced(project)
        dto?.humanReadableProjectName = project.attributes[ProjectDTO.HUMAN_READABLE_PROJECT_NAME]
        dto?.sourceTypes = emptySet()
        return dto
    }

    override fun projectDTOToProject(projectDto: ProjectDTO?): Project? {
        if (projectDto == null) {
            return null
        }
        val project = delegate.projectDTOToProject(projectDto)
        val projectName = projectDto.humanReadableProjectName
        if (!projectName.isNullOrEmpty()) {
            project!!.attributes[ProjectDTO.HUMAN_READABLE_PROJECT_NAME] = projectName
        }

        val name = projectDto.organizationName
        if (name != null && projectDto.organization != null) {
            val org = organizationRepository.findOneByName(name)
                ?: throw NotFoundException(
                        "Organization not found with name",
                        EntityName.ORGANIZATION,
                        ErrorConstants.ERR_ORGANIZATION_NAME_NOT_FOUND,
                        Collections.singletonMap("name", name)
                    )
            project!!.organization = org
        }
        return project
    }

    override fun descriptiveDTOToProject(minimalProjectDetailsDto: MinimalProjectDetailsDTO?): Project? {
        return if (minimalProjectDetailsDto == null) {
            null
        } else minimalProjectDetailsDto.id?.let { projectRepository.getById(it) }
    }
}
