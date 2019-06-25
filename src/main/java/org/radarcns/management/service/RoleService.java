package org.radarcns.management.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class RoleService {

    private final Logger log = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserService userService;

    /**
     * Save a role.
     *
     * @param roleDto the entity to save
     * @return the persisted entity
     */
    public RoleDTO save(RoleDTO roleDto) {
        log.debug("Request to save Role : {}", roleDto);
        Role role = roleMapper.roleDTOToRole(roleDto);
        role = roleRepository.save(role);
        return roleMapper.roleToRoleDTO(role);
    }

    /**
     * Get the roles the currently authenticated user has access to.
     *
     * <p>A system administrator has access to all the roles. A project administrator has access
     * to the roles in their own project.</p>
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> findAll() {
        User currentUser = userService.getUserWithAuthorities();
        if (currentUser == null) {
            // return an empty list if we do not have a current user (e.g. with client credentials
            // oauth2 grant)
            return Collections.emptyList();
        }
        List<String> currentUserAuthorities = currentUser.getAuthorities().stream()
                .map(Authority::getName).collect(Collectors.toList());
        if (currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN)) {
            log.debug("Request to get all Roles");
            return roleRepository.findAll().stream()
                    .map(roleMapper::roleToRoleDTO)
                    .collect(Collectors.toList());
        } else if (currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get project admin's project Projects");
            return currentUser.getRoles().stream()
                    .filter(role -> AuthoritiesConstants.PROJECT_ADMIN
                            .equals(role.getAuthority().getName()))
                    .map(r -> r.getProject().getProjectName())
                    .distinct()
                    .flatMap(name -> roleRepository.findAllRolesByProjectName(name).stream())
                    .map(roleMapper::roleToRoleDTO)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get all Admin roles.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> findSuperAdminRoles() {
        log.debug("Request to get admin Roles");

        return roleRepository
                .findRolesByAuthorityName(AuthoritiesConstants.SYS_ADMIN).stream()
                .map(roleMapper::roleToRoleDTO)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one role by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RoleDTO findOne(Long id) {
        log.debug("Request to get Role : {}", id);
        Role role = roleRepository.findOne(id);
        return roleMapper.roleToRoleDTO(role);
    }

    /**
     * Delete the  role by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Role : {}", id);
        roleRepository.delete(id);
    }

    /**
     * Get all roles related to a project.
     * @param projectName the project name
     * @return the roles
     */
    public List<RoleDTO> getRolesByProject(String projectName) {
        log.debug("Request to get all Roles for projectName " + projectName);

        return roleRepository.findAllRolesByProjectName(projectName).stream()
                .map(roleMapper::roleToRoleDTO)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get the role related to the given project with the given authority name.
     * @param projectName the project name
     * @param authorityName the authority name
     * @return an {@link Optional} containing the role if it exists, and empty otherwise
     */
    public Optional<RoleDTO> findOneByProjectNameAndAuthorityName(String projectName,
            String authorityName) {
        log.debug("Request to get role of project {} and authority {}", projectName, authorityName);
        return roleRepository.findOneByProjectNameAndAuthorityName(projectName, authorityName)
                .map(roleMapper::roleToRoleDTO);
    }
}
