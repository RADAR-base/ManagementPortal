package org.radarcns.management.service;


import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.Project;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class ProjectService {

    private final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * Save a project.
     *
     * @param projectDTO the entity to save
     * @return the persisted entity
     */
    public ProjectDTO save(ProjectDTO projectDTO) {
        log.debug("Request to save Project : {}", projectDTO);
        Project project = projectMapper.projectDTOToProject(projectDTO);
        project = projectRepository.save(project);
        ProjectDTO result = projectMapper.projectToProjectDTO(project);
        return result;
    }

    /**
     *  Get all the projects.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List findAll(Boolean fetchMinimal) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        List<String> currentUserAuthorities = authentication.getAuthorities().stream()
//            .map(GrantedAuthority::getAuthority)
//            .collect(Collectors.toList());
//
//        List<Project> projects = new LinkedList<>();
//        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN) ||
//            currentUserAuthorities.contains(AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR)) {
//            log.debug("Request to get all Projects");
//            projects = projectRepository.findAllWithEagerRelationships();
//        }
//        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
//            log.debug("Request to get project admin's project Projects");
//            String name = authentication.getName();
//            Optional<UserDTO> user = userService.getUserWithAuthoritiesByLogin(name);
//            if (user.isPresent()) {
//                User currentUser = userMapper.userDTOToUser(user.get());
//                List<Role> pAdminRoles = currentUser.getRoles().stream()
//                    // get all roles that are a PROJECT_ADMIN role
//                    .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PROJECT_ADMIN))
//                    .collect(Collectors.toList());
//                pAdminRoles.stream()
//                    .forEach(r -> log.debug("Found PROJECT_ADMIN role for project with id {}",
//                        r.getProject().getId()));
//                projects.addAll(pAdminRoles.stream()
//                    // map them into projects
//                    .map(r -> projectRepository.findOneWithEagerRelationships(r.getProject().getId()))
//                    .collect(Collectors.toList()));
//            }
//            else {
//                log.debug("Could find a user with name {}", name);
//            }
//        }

        List<Project> projects = projectRepository.findAllWithEagerRelationships();
        if(!fetchMinimal){
            return projectMapper.projectsToProjectDTOs(projects);
        } else {
            return projectMapper.projectsToMinimalProjectDetailsDTOs(projects);
        }
    }

    /**
     *  Get one project by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOne(Long id) {
        log.debug("Request to get Project : {}", id);
        Project project = projectRepository.findOneWithEagerRelationships(id);
        ProjectDTO projectDTO = projectMapper.projectToProjectDTO(project);
        return projectDTO;
    }

    /**
     *  Get one project by name.
     *
     *  @param name the name of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectDTO findOneByName(String name) {
        log.debug("Request to get Project by name: {}", name);
        Project project = projectRepository.findOneWithEagerRelationshipsByName(name);
        ProjectDTO projectDTO = projectMapper.projectToProjectDTO(project);
        return projectDTO;
    }

    /**
     *  Get one project by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public List<SourceTypeDTO> findSourceTypesById(Long id) {
        log.debug("Request to get Project.sourceTypes of project: {}", id);
        List<SourceType> sourceTypes = projectRepository.findSourceTypesByProjectId(id);
        return sourceTypeMapper.sourceTypesToSourceTypeDTOs(sourceTypes);
    }

    /**
     *  Delete the  project by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        projectRepository.delete(id);
    }
}
