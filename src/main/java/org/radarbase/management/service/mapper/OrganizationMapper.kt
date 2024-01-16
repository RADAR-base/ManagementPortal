package org.radarbase.management.service.mapper

import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.radarbase.management.domain.Organization
import org.radarbase.management.service.dto.OrganizationDTO

/**
 * Mapper for the entity Organization and its DTO OrganizationDTO.
 */
@Mapper(componentModel = "spring", uses = [ProjectMapper::class])
interface OrganizationMapper {
    @Named("organizationToOrganizationDTO")
    @Mapping(target = "projects", qualifiedByName = ["projectReducedDTO"])
    fun organizationToOrganizationDTO(organization: Organization): OrganizationDTO

    @Named("organizationToOrganizationDTOWithoutProjects")
    @Mapping(target = "projects", ignore = true)
    fun organizationToOrganizationDTOWithoutProjects(organization: Organization): OrganizationDTO

    @Mapping(target = "projects", ignore = true)
    fun organizationDTOToOrganization(organizationDto: OrganizationDTO): Organization

    @IterableMapping(qualifiedByName = ["organizationToOrganizationDTO"])
    fun organizationsToOrganizationDTOs(organizations: List<Organization>): List<OrganizationDTO>
}
