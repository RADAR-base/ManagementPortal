package org.radarbase.management.service;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.OrganizationRepository;
import org.radarbase.management.service.dto.OrganizationDTO;
import org.radarbase.management.service.mapper.OrganizationMapper;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.radarbase.auth.authorization.Permission.ORGANIZATION_READ;
import static org.radarbase.management.web.rest.errors.EntityName.USER;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_ENTITY_NOT_FOUND;

/**
 * Service Implementation for managing Organization.
 */
@Service
@Transactional
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private RadarToken token;

    /**
     * Save an organization.
     *
     * @param organizationDto the entity to save
     * @return the persisted entity
     */
    public OrganizationDTO save(OrganizationDTO organizationDto) {
        log.debug("Request to save Organization : {}", organizationDto);
        var org = organizationMapper.organizationDTOToOrganization(organizationDto);
        org = organizationRepository.save(org);
        return organizationMapper.organizationToOrganizationDTO(org);
    }

    /**
     * Get all the organizations.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<OrganizationDTO> findAll() {
        Stream<Organization> organizationsOfUser;

        if (token.getGlobalRoles().stream().anyMatch(ORGANIZATION_READ::isRoleAllowed)) {
            organizationsOfUser = organizationRepository.findAll().stream();
        } else {
            List<String> projectNames = token.getProjectRoles().entrySet().stream()
                    .filter(e -> e.getValue().stream().anyMatch(ORGANIZATION_READ::isRoleAllowed))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            List<Organization> organizationsOfProject = organizationRepository
                    .findAllByProjectNames(projectNames);

            Stream<Organization> organizationsOfRole = token.getOrganizationRoles().entrySet()
                    .stream()
                    .filter(e -> e.getValue().stream().anyMatch(ORGANIZATION_READ::isRoleAllowed))
                    .map(Map.Entry::getKey)
                    .map(name -> organizationRepository.findOneByName(name).orElse(null))
                    .filter(Objects::nonNull);

            organizationsOfUser = Stream.concat(
                    organizationsOfRole, organizationsOfProject.stream()).distinct();
        }

        return organizationsOfUser
            .map(organizationMapper::organizationToOrganizationDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get one organization by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<OrganizationDTO> findByName(String name) {
        log.debug("Request to get Organization by name: {}", name);
        return organizationRepository.findOneByName(name)
                .map(organizationMapper::organizationToOrganizationDTO);
    }
}
