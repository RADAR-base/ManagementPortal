package org.radarbase.management.service.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.service.dto.OrganizationDTO;

import java.util.List;

/**
 * Mapper for the entity Organization and its DTO OrganizationDTO.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    @Named("organizationToOrganizationDTO")
    OrganizationDTO organizationToOrganizationDTO(Organization organization);

    Organization organizationDTOToOrganization(OrganizationDTO organizationDto);

    @IterableMapping(qualifiedByName = "organizationToOrganizationDTO")
    List<OrganizationDTO> organizationsToOrganizationDTOs(List<Organization> organizations);

}
