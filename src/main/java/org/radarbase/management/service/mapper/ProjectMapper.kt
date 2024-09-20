package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.radarbase.management.domain.Project
import org.radarbase.management.service.dto.MinimalProjectDetailsDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.PublicProjectDTO
import org.radarbase.management.service.mapper.decorator.ProjectMapperDecorator

/**
 * Mapper for the entity Project and its DTO ProjectDTO.
 */
@Mapper(componentModel = "spring", uses = [GroupMapper::class, SourceTypeMapper::class, OrganizationMapper::class])
@DecoratedWith(
    ProjectMapperDecorator::class,
)
interface ProjectMapper {
    @Mapping(target = "humanReadableProjectName", ignore = true)
    @Mapping(
        target = "organization",
        source = "organization",
        qualifiedByName = ["organizationToOrganizationDTOWithoutProjects"],
    )
    @Mapping(target = "persistentTokenTimeout", ignore = true)
    @Mapping(target = "groups", qualifiedByName = ["groupToGroupDTO"])
    @Mapping(target = "sourceTypes", qualifiedByName = ["sourceTypeToSourceTypeDTOReduced"])
    fun projectToProjectDTO(project: Project?): ProjectDTO?

    @Named(value = "projectReducedDTO")
    @Mapping(target = "humanReadableProjectName", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "sourceTypes", ignore = true)
    @Mapping(target = "persistentTokenTimeout", ignore = true)
    @Mapping(target = "groups", qualifiedByName = ["groupToGroupDTO"])
    fun projectToProjectDTOReduced(project: Project?): ProjectDTO?

    @IterableMapping(qualifiedByName = ["projectReducedDTO"])
    fun projectsToProjectDTOs(projects: List<Project>): List<ProjectDTO>

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "organization", ignore = true)
    fun projectDTOToProject(projectDto: ProjectDTO?): Project?

    fun projectDTOsToProjects(projectDtos: List<ProjectDTO>): List<Project>

    fun projectToMinimalProjectDetailsDTO(project: Project): MinimalProjectDetailsDTO

    fun projectsToMinimalProjectDetailsDTOs(projects: List<Project>): List<MinimalProjectDetailsDTO>

    fun projectToPublicProjectDTO(project: Project): PublicProjectDTO

    @Mapping(target = "description", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "organizationName", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "projectStatus", ignore = true)
    @Mapping(target = "sourceTypes", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "groups", ignore = true)
    fun descriptiveDTOToProject(minimalProjectDetailsDto: MinimalProjectDetailsDTO?): Project?

    fun descriptiveDTOsToProjects(minimalProjectDetailsDtos: List<MinimalProjectDetailsDTO>): List<Project>
}
