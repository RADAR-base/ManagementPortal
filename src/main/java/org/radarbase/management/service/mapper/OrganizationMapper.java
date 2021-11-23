package org.radarbase.management.service.mapper;

import org.mapstruct.Mapper;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.service.dto.OrganizationDTO;

/**
 * Mapper for the entity Organization and its DTO OrganizationDTO.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationDTO organizationToOrganizationDTO(Organization organization);

    Organization organizationDTOToOrganization(OrganizationDTO organizationDto);
}
