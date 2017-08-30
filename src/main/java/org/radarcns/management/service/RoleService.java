package org.radarcns.management.service;

import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param roleDTO the entity to save
     * @return the persisted entity
     */
    public RoleDTO save(RoleDTO roleDTO) {
        log.debug("Request to save Role : {}", roleDTO);
        Role role = roleMapper.roleDTOToRole(roleDTO);
        role = roleRepository.save(role);
        RoleDTO result = roleMapper.roleToRoleDTO(role);
        return result;
    }

    /**
     *  Get all the roles.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> findAll() {
        List<RoleDTO> result = new LinkedList<>();
        User currentUser = userService.getUserWithAuthorities();
        List<String> currentUserAuthorities = currentUser.getAuthorities().stream().map(Authority::getName).collect(
            Collectors.toList());
        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN)) {
            log.debug("Request to get all Roles");
            result = roleRepository.findAll().stream()
                .map(roleMapper::roleToRoleDTO)
                .collect(Collectors.toCollection(LinkedList::new));
        }
        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get project admin's project Projects");
//            result =  roleRepository.findAllRolesByProjectId(currentUser.getProject().getId()).stream()
//                .map(roleMapper::roleToRoleDTO)
//                .collect(Collectors.toCollection(LinkedList::new));
        }
        return result;
    }

    /**
     *  Get all Admin roles.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> findSuperAdminRoles() {
        log.debug("Request to get admin Roles");
        List<RoleDTO> result = roleRepository.findRolesByAuthorityName(AuthoritiesConstants.SYS_ADMIN).stream()
            .map(roleMapper::roleToRoleDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    /**
     *  Get one role by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public RoleDTO findOne(Long id) {
        log.debug("Request to get Role : {}", id);
        Role role = roleRepository.findOne(id);
        RoleDTO roleDTO = roleMapper.roleToRoleDTO(role);
        return roleDTO;
    }

    /**
     *  Delete the  role by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Role : {}", id);
        roleRepository.delete(id);
    }

    public List<RoleDTO> getRolesByProject(Long projectId) {
        log.debug("Request to get all Roles for projectId " +projectId);
        List<RoleDTO> result = roleRepository.findAllRolesByProjectId(projectId).stream()
            .map(roleMapper::roleToRoleDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }
}
