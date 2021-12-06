package org.radarbase.management.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.AuthorityRepository;
import org.radarbase.management.repository.OrganizationRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.RoleRepository;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.service.mapper.RoleMapper;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.radarbase.management.web.rest.errors.EntityName.USER;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

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

        if (currentUserAuthorities.contains(RoleAuthority.SYS_ADMIN.authority())) {
            log.debug("Request to get all Roles");
            return roleRepository.findAll().stream()
                    .map(roleMapper::roleToRoleDTO)
                    .collect(Collectors.toList());
        } else if (currentUserAuthorities.contains(RoleAuthority.PROJECT_ADMIN.authority())) {
            log.debug("Request to get project admin's project Projects");
            return currentUser.getRoles().stream()
                    .filter(role -> RoleAuthority.PROJECT_ADMIN.authority()
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
                .findRolesByAuthorityName(RoleAuthority.SYS_ADMIN.authority()).stream()
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
        Role role = roleRepository.findById(id).get();
        return roleMapper.roleToRoleDTO(role);
    }

    /**
     * Delete the  role by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Role : {}", id);
        roleRepository.deleteById(id);
    }

    /**
     * Get the predefined role authority from a RoleDTO.
     * @param roleDto roleDto to parse
     * @return role authority
     * @throws BadRequestException if the roleauthority is not found or does not correctly
     *                             specify an organization or project ID.
     */
    public static RoleAuthority getRoleAuthority(RoleDTO roleDto) {
        RoleAuthority authority;
        try {
            authority = RoleAuthority.valueOfAuthority(roleDto.getAuthorityName());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Authority not found with "
                    + "authorityName", USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap("authorityName",
                            roleDto.getAuthorityName()));
        }
        if (authority.scope() == RoleAuthority.Scope.ORGANIZATION
                && roleDto.getOrganizationId() == null) {
            throw new BadRequestException("Authority with "
                    + "authorityName should have organization ID",
                    USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap("authorityName", roleDto.getAuthorityName()));
        }
        if (authority.scope() == RoleAuthority.Scope.PROJECT
                && roleDto.getProjectId() == null) {
            throw new BadRequestException("Authority with "
                    + "authorityName should have project ID",
                    USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap("authorityName", roleDto.getAuthorityName()));
        }
        return authority;
    }

    /**
     * Get or create given global role.
     * @param role to get or create
     * @return role from database
     */
    public Role getGlobalRole(RoleAuthority role) {
        return roleRepository.findRolesByAuthorityName(role.authority()).stream()
                .findAny()
                .orElseGet(() -> createNewRole(role, r -> { }));
    }

    /**
     * Get or create given organization role.
     * @param role to get or create
     * @param organizationId organization ID
     * @return role from database
     */
    public Role getOrganizationRole(RoleAuthority role, Long organizationId) {
        return roleRepository.findOneByOrganizationIdAndAuthorityName(
                        organizationId, role.authority())
                .orElseGet(() -> createNewRole(role, r -> {
                    r.setOrganization(organizationRepository.findById(organizationId)
                            .orElseThrow(() -> new NotFoundException(
                                    "Cannot find organization for authority",
                                    USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                                    Map.of("authorityName", role.authority(),
                                            "projectId",
                                            organizationId.toString()))));
                }));
    }

    /**
     * Get or create given project role.
     * @param role to get or create
     * @param projectId organization ID
     * @return role from database
     */
    public Role getProjectRole(RoleAuthority role, Long projectId) {
        return roleRepository.findOneByProjectIdAndAuthorityName(
                        projectId, role.authority())
                .orElseGet(() -> createNewRole(role, r -> {
                    r.setProject(projectRepository.findById(projectId)
                            .orElseThrow(() -> new NotFoundException(
                                    "Cannot find project for authority",
                                    USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                                    Map.of("authorityName", role.authority(),
                                            "projectId",
                                            projectId.toString()))));
                }));
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

    private Authority getAuthority(RoleAuthority role) {
        return authorityRepository.findByAuthorityName(role.authority())
                .orElseGet(() -> {
                    var a = new Authority(role);
                    authorityRepository.saveAndFlush(a);
                    return a;
                });
    }

    private Role createNewRole(RoleAuthority role, Consumer<Role> apply) {
        Role newRole = new Role();
        newRole.setAuthority(getAuthority(role));
        apply.accept(newRole);
        roleRepository.save(newRole);
        return newRole;
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
