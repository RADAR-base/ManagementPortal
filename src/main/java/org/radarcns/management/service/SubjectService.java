package org.radarcns.management.service;

import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.MinimalProjectDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nivethika on 26-5-17.
 */
@Service
@Transactional
public class SubjectService {

    private final Logger log = LoggerFactory.getLogger(SubjectService.class);

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;


    @Transactional
    public SubjectDTO createSubject(SubjectDTO subjectDTO) {
        Subject subject = subjectMapper.subjectDTOToSubject(subjectDTO);
        //assign roles
        User user = subject.getUser();
        user.setRoles(getProjectParticipantRole(subjectDTO.getProject()));

        // set password and reset keys
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setLangKey(
            "en"); // setting default language key to "en", required to set email context,
        // Find a workaround
        user.setResetDate(ZonedDateTime.now());
        // default subject is deactivated. @TODO can be activated when the QRCode flow is finalized
        user.setActivated(false);
        //set if any devices are set as assigned
        if(subject.getSources() !=null && !subject.getSources().isEmpty()) {
            for (Source source : subject.getSources()) {
                source.setAssigned(true);
            }
        }
        subject = subjectRepository.save(subject);
        if (subject.getId() != null) {
            mailService.sendCreationEmailForGivenEmail(subject.getUser(), subjectDTO.getEmail());
        }
        return subjectMapper.subjectToSubjectDTO(subject);
    }

    /**
     * fetch Participant role of the project if available, otherwise create a new Role and assign
     * @param projectDTO project subject is assigned to
     * @return relevant Participant role
     */
    private Set<Role> getProjectParticipantRole(ProjectDTO projectDTO) {

        Set<Role> roles = new HashSet<>();

        Role role = roleRepository
            .findOneByAuthorityNameAndProjectId(AuthoritiesConstants.PARTICIPANT,
                projectDTO.getId());
        if (role != null) {
            roles.add(role);
        } else {
            Role subjectRole = new Role();
            subjectRole.setAuthority(
                authorityRepository.findByAuthorityName(AuthoritiesConstants.PARTICIPANT));
            subjectRole.setProject(projectMapper.projectDTOToProject(projectDTO));
            roleRepository.save(subjectRole);
            roles.add(subjectRole);
        }
        return roles;
    }


    @Transactional
    public SubjectDTO updateSubject(SubjectDTO subjectDTO) throws IllegalAccessException {
        if (subjectDTO.getId() == null) {
            return createSubject(subjectDTO);
        }
        //  TODO : add security and owner check for the resource
        Subject subject = subjectRepository.findOne(subjectDTO.getId());
        //reset all the sources assigned to a subject to unassigned
        for(Source source : subject.getSources()) {
            source.setAssigned(false);
            sourceRepository.save(source);
        }
        //set only the devices assigned to a subject as assigned
        subjectMapper.safeUpdateSubjectFromDTO(subjectDTO, subject);
        for(Source source : subject.getSources()) {
            source.setAssigned(true);
        }
        // update role
        Set<Role> managedRoles = subject.getUser().getRoles();
        managedRoles.clear();
        managedRoles.addAll(getProjectParticipantRole(subjectDTO.getProject()));
        subject = subjectRepository.save(subject);

        return subjectMapper.subjectToSubjectDTO(subject);
    }


    public List<SubjectDTO> findAll() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> currentUserAuthorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        List<Subject> subjects = new LinkedList<>();
        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN) ||
                currentUserAuthorities.contains(AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR)) {
            log.debug("Request to get all subjects");
            subjects = subjectRepository.findAllWithEagerRelationships();
        }
        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get subjects of admin's project ");
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
                subjects.addAll(pAdminRoles.stream()
                    // map them into a list of subjects for that project
                    .map(r -> subjectRepository.findAllByProjectId(r.getProject().getId()))
                    // we have a list of lists of subjects, so flatten them into a single list
                    .flatMap(List::stream)
                    .collect(Collectors.toList()));
            }
            else {
                log.debug("Could find a user with name {}", name);
            }
            log.debug("Request to get Sources of admin's project ");
//            subjects = subjectRepository.findAllByProjectId(currentUser.getProject().getId());
        }
        return subjectMapper.subjectsToSubjectDTOs(subjects);
    }

    public SubjectDTO discontinueSubject(SubjectDTO subjectDTO) {
        Subject subject = subjectRepository.findOne(subjectDTO.getId());
        //reset all the sources assigned to a subject to unassigned
        for (Source source : subject.getSources()) {
            source.setAssigned(false);
            sourceRepository.save(source);
        }

        subject.setRemoved(true);
        return subjectMapper.subjectToSubjectDTO(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectDTO assignSourcesToSubject(Subject subject, List<SourceDTO> sourceDTOS) {
        log.debug("User {} is registering {} sources", subject.getUser().getLogin(), sourceDTOS.size());

        // find out project this subject is participant in
        Optional<Project> project = subject.getUser().getRoles().stream()
            .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PARTICIPANT))
            .map(r -> r.getProject())
            .findFirst();

        if (project.isPresent()) {
            MinimalProjectDetailsDTO minimalProjectDetailsDTO =
                    projectMapper.projectToMinimalProjectDetailsDTO(project.get());
            sourceDTOS.forEach(s -> s.setProject(minimalProjectDetailsDTO));
        }

        // set these sources assigned states before saving
        sourceDTOS.forEach(s -> s.setAssigned(true));

        // save the supplied sources
        List<SourceDTO> savedSources = new ArrayList<>(sourceDTOS.size());
        sourceDTOS.forEach(s -> savedSources.add(sourceService.save(s)));

        // add the sources to the subject and save the subject
        List<Source> sources = sourceMapper.sourceDTOsToSources(savedSources);
        subject.setSources(new HashSet<>(sources));
        subject = subjectRepository.save(subject);

        // transform to DTO and return the result
        return subjectMapper.subjectToSubjectDTO(subject);
    }
}
