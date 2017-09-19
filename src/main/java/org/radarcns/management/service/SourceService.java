package org.radarcns.management.service;

import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Source.
 */
@Service
@Transactional
public class SourceService {

    private Logger log = LoggerFactory.getLogger(SourceService.class);

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * Save a Source.
     *
     * @param sourceDTO the entity to save
     * @return the persisted entity
     */
    public SourceDTO save(SourceDTO sourceDTO) {
        log.debug("Request to save Source : {}", sourceDTO);
        // make sure we update the correct entity if id is not specified but sourceName or sourceId
        // is, by setting the id of the object we're going to save
        if (sourceDTO.getSourceName() != null && sourceDTO.getId() == null) {
            Optional<Source> sourceOptional = sourceRepository.findOneBySourceName(sourceDTO.getSourceName());
            if (sourceOptional.isPresent()) {
                sourceDTO.setId(sourceOptional.get().getId());
            }
        }
        if (sourceDTO.getSourceId() != null && sourceDTO.getId() == null) {
            Optional<Source> sourceOptional = sourceRepository.findOneBySourceId(sourceDTO.getSourceId());
            if (sourceOptional.isPresent()) {
                sourceDTO.setId(sourceOptional.get().getId());
            }
        }
        Source source = sourceMapper.sourceDTOToSource(sourceDTO);

        // Find the full device type based on the supplied id
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.findOne(source.getDeviceType().getId());

        // Find the full project based on the supplied id or supplied name
        if (source.getProject() != null ) {
            ProjectDTO project = null;
            if (source.getProject().getId() != null) {
                project = projectService.findOne(source.getProject().getId());
            } else if (source.getProject().getProjectName() != null) {
                project = projectService.findOneByName(source.getProject().getProjectName());
            }
            source.setProject(projectMapper.projectDTOToProject(project));
        }
        // generate defaults for source id and source name if they were not provided
        UUID uuid = UUID.randomUUID();
        if (source.getSourceName() == null) {
            source.setSourceName(deviceTypeDTO.getDeviceModel()
                + "-" + uuid.toString().substring(0,5));
        }
        if (source.getSourceId() == null) {
            source.setSourceId(uuid);
        }

        // save the source
        source = sourceRepository.save(source);

        // prepare the saved source to be returned to the client
        SourceDTO result = sourceMapper.sourceToSourceDTO(source);

        // add the full details of the deviceType to the response
        result.setDeviceType(deviceTypeDTO);
        return result;
    }

    /**
     *  Get all the Sources.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> findAll() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        List<String> currentUserAuthorities = authentication.getAuthorities().stream()
//            .map(GrantedAuthority::getAuthority)
//            .collect(Collectors.toList());
//
//        List<Source> sources = new LinkedList<>();
//        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN) ||
//            currentUserAuthorities.contains(AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR)) {
//            log.debug("Request to get all Sources");
//            sources = sourceRepository.findAll();
//        }
//        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
//            log.debug("Request to get Sources of admin's project ");
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
//                sources.addAll(pAdminRoles.stream()
//                    // map them into a list of sources for that project
//                    .map(r -> sourceRepository.findAllSourcesByProjectId(r.getProject().getId()))
//                    // we have a list of lists of sources, so flatten them into a single list
//                    .flatMap(List::stream)
//                    .collect(Collectors.toList()));
//            }
//            else {
//                log.debug("Could find a user with name {}", name);
//            }
//        }
//        log.debug("Request to get all Sources");
//        List<SourceDTO> result = sources.stream()
        return sourceRepository.findAll()
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Source> findAllUnassignedSources() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> currentUserAuthorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        List<Source> sources = new LinkedList<>();
        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN) ||
            currentUserAuthorities.contains(AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR)) {
            log.debug("Request to get all Sources");
            sources = sourceRepository.findAllSourcesByAssigned(false);
        }
        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get Sources of admin's project ");
            String name = authentication.getName();
            Optional<UserDTO> user = userService.getUserWithAuthoritiesByLogin(name);
            if (user.isPresent()) {
                User currentUser = userMapper.userDTOToUser(user.get());
                List<Role> pAdminRoles = currentUser.getRoles().stream()
                    // get all roles that are a PROJECT_ADMIN role
                    .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PROJECT_ADMIN))
                    .collect(Collectors.toList());
                pAdminRoles.stream()
                    .forEach(r -> log.debug("Found PROJECT_ADMIN role for project with id {}",
                        r.getProject().getId()));
                sources.addAll(pAdminRoles.stream()
                    // map them into a list of sources for that project
                    .map(r -> sourceRepository.findAllSourcesByProjectIdAndAssigned(
                        r.getProject().getId(), false))
                    // we have a list of lists of sources, so flatten them into a single list
                    .flatMap(List::stream)
                    .collect(Collectors.toList()));
            }
            else {
                log.debug("Could find a user with name {}", name);
            }
//            sources = sourceRepository.findAllSourcesByProjectIdAndAssigned(currentUser.getProject().getId(), false);
        }
        return sources;
    }
    public List<MinimalSourceDetailsDTO> findAllUnassignedSourcesMinimalDTO() {

        return findAllUnassignedSources().stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<MinimalSourceDetailsDTO> findAllUnassignedSourcesAndOfSubject(Long id) {
        log.debug("Request to get all unassigned sources and assigned sources of a subject");
        List<Source> subjectSources = subjectRepository.findSourcesBySubjectId(id);
        List<Source> sources = findAllUnassignedSources();
        sources.addAll(subjectSources);
        List<MinimalSourceDetailsDTO> result = sources.stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
        return result;
    }
    /**
     *  Get one device by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SourceDTO findOne(Long id) {
        log.debug("Request to get Source : {}", id);
        Source source = sourceRepository.findOne(id);
        SourceDTO sourceDTO = sourceMapper.sourceToSourceDTO(source);
        return sourceDTO;
    }

    /**
     *  Delete the  device by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Source : {}", id);
        sourceRepository.delete(id);
    }

    public List<SourceDTO> findAllByProjectId(Long projectId) {
        return sourceRepository.findAllSourcesByProjectId(projectId)
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
