package org.radarbase.management.service;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.repository.OrganizationRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.service.dto.OrganizationDTO;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.mapper.OrganizationMapper;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.radarbase.auth.authorization.Permission.ORGANIZATION_READ;

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
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private RadarToken token;

    @Autowired
    private ProjectMapper projectMapper;

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

        if (token.hasGlobalAuthorityForPermission(ORGANIZATION_READ)) {
            organizationsOfUser = organizationRepository.findAll().stream();
        } else {
            List<String> projectNames = token.getReferentsWithPermission(
                    RoleAuthority.Scope.PROJECT, ORGANIZATION_READ)
                    .collect(Collectors.toList());

            Stream<Organization> organizationsOfProject = projectNames.isEmpty()
                    ? Stream.of()
                    : organizationRepository.findAllByProjectNames(projectNames).stream();

            Stream<Organization> organizationsOfRole = token.getReferentsWithPermission(
                    RoleAuthority.Scope.ORGANIZATION, ORGANIZATION_READ)
                    .flatMap(name -> organizationRepository.findOneByName(name).stream())
                    .filter(Objects::nonNull);

            organizationsOfUser = Stream.concat(organizationsOfRole, organizationsOfProject)
                    .distinct();
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

    /**
     * Get all projects belonging to the organization.
     *
     * @return the list of projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAllProjectsByOrganizationName(String organizationName) {
        return projectRepository.findAllByOrganizationName(organizationName).stream()
                .filter(project -> token.hasPermissionOnOrganizationAndProject(
                        ORGANIZATION_READ, organizationName, project.getProjectName()))
                .map(projectMapper::projectToProjectDTO)
                .collect(Collectors.toList());
    }
}