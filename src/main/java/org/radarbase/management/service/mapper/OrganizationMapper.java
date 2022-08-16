package org.radarbase.management.service.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.service.dto.OrganizationDTO;

import java.util.List;

/**
 * Mapper for the entity Organization and its DTO OrganizationDTO.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class})
public interface OrganizationMapper {
    @Named("organizationToOrganizationDTO")
    @Mapping(target = "projects", qualifiedByName = "projectReducedDTO")
    OrganizationDTO organizationToOrganizationDTO(Organization organization);

    @Named("organizationToOrganizationDTOWithoutProjects")
    @Mapping(target = "projects", ignore = true)
    OrganizationDTO organizationToOrganizationDTOWithoutProjects(Organization organization);

    @Mapping(target = "projects", ignore = true)
    Organization organizationDTOToOrganization(OrganizationDTO organizationDto);

    @IterableMapping(qualifiedByName = "organizationToOrganizationDTO")
    List<OrganizationDTO> organizationsToOrganizationDTOs(List<Organization> organizations);
}
