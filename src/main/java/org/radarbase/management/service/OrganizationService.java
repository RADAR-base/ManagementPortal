package org.radarbase.management.service;

import org.radarbase.management.domain.Organization;
import org.radarbase.management.domain.Project;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private ProjectMapper projectMapper;

    @Autowired
    private AuthService authService;

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
        List<Organization> organizationsOfUser;

        var referents = authService.referentsByScope(ORGANIZATION_READ);

        if (referents.getGlobal()) {
            organizationsOfUser = organizationRepository.findAll();
        } else {
            Set<String> projectNames = referents.getAllProjects();

            Stream<Organization> organizationsOfProject = !projectNames.isEmpty()
                    ? organizationRepository.findAllByProjectNames(projectNames).stream()
                    : Stream.of();

            Stream<Organization> organizationsOfRole = referents.getOrganizations()
                    .stream()
                    .flatMap(name -> organizationRepository.findOneByName(name).stream());

            organizationsOfUser = Stream.concat(organizationsOfRole, organizationsOfProject)
                    .distinct()
                    .toList();
        }

        return organizationMapper.organizationsToOrganizationDTOs(organizationsOfUser);
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
        var referents = authService.referentsByScope(ORGANIZATION_READ);
        if (referents.isEmpty()) {
            return Collections.emptyList();
        }

        Stream<Project> projectStream;

        if (referents.getGlobal() || referents.hasOrganization(organizationName)) {
            projectStream = projectRepository.findAllByOrganizationName(organizationName).stream();
        } else if (referents.hasAnyProjects()) {
            projectStream = projectRepository.findAllByOrganizationName(organizationName).stream()
                    .filter(project -> referents.hasAnyProject(project.getProjectName()));
        } else {
            return List.of();
        }

        return projectStream
                .map(projectMapper::projectToProjectDTO)
                .toList();
    }
}
