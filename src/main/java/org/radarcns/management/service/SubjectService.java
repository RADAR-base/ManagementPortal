package org.radarcns.management.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ProjectMapper projectMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public SubjectDTO createSubject(SubjectDTO subjectDTO) {
        Subject subject = subjectMapper.subjectDTOToSubject(subjectDTO);
        //assign roles
        User user = subject.getUser();
        user.getRoles().add(getProjectParticipantRole(subjectDTO.getProject()));

        // set password and reset keys
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setLangKey(
            "en"); // setting default language key to "en", required to set email context,
        // Find a workaround
        user.setResetDate(ZonedDateTime.now());
        // default subject is activated.
        user.setActivated(true);
        //set if any devices are set as assigned
        if(subject.getSources() !=null && !subject.getSources().isEmpty()) {
            for (Source source : subject.getSources()) {
                source.setAssigned(true);
            }
        }
        subject = subjectRepository.save(subject);
        return subjectMapper.subjectToSubjectDTO(subject);
    }

    /**
     * fetch Participant role of the project if available, otherwise create a new Role and assign
     * @param projectDTO project subject is assigned to
     * @return relevant Participant role
     */
    private Role getProjectParticipantRole(ProjectDTO projectDTO) {

        Role role = roleRepository
            .findOneByProjectIdAndAuthorityName(projectDTO.getId(),
                AuthoritiesConstants.PARTICIPANT);
        if (role != null) {
            return role;
        } else {
            Role subjectRole = new Role();
            subjectRole.setAuthority(
                authorityRepository.findByAuthorityName(AuthoritiesConstants.PARTICIPANT));
            subjectRole.setProject(projectMapper.projectDTOToProject(projectDTO));
            roleRepository.save(subjectRole);
            return subjectRole;
        }
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
        // update participant role
        Set<Role> managedRoles = subject.getUser().getRoles().stream()
            .filter(r -> !AuthoritiesConstants.PARTICIPANT.equals(r.getAuthority().getName()))
            .collect(Collectors.toSet());
        managedRoles.add(getProjectParticipantRole(subjectDTO.getProject()));
        subject.getUser().setRoles(managedRoles);
        subject = subjectRepository.save(subject);

        return subjectMapper.subjectToSubjectDTO(subject);
    }


    public List<SubjectDTO> findAll() {
        return subjectMapper.subjectsToSubjectDTOs(subjectRepository.findAllWithEagerRelationships());
    }

    public SubjectDTO discontinueSubject(SubjectDTO subjectDTO) {
        Subject subject = subjectRepository.findOne(subjectDTO.getId());
        // reset all the sources assigned to a subject to unassigned
        unassignAllSources(subject);

        // set the removed flag and deactivate the user to prevent them from refreshing their
        // access token
        subject.setRemoved(true);
        subject.getUser().setActivated(false);
        return subjectMapper.subjectToSubjectDTO(subjectRepository.save(subject));
    }

    /**
     * Unassign all sources from a subject. This method saves the unassigned sources, but does
     * NOT save the subject in question. This is the responsibility of the caller.
     * @param subject The subject for which to unassign all sources
     */
    private void unassignAllSources(Subject subject) {
        subject.getSources().forEach(source -> {
            source.setAssigned(false);
            sourceRepository.save(source);
        });
        subject.getSources().clear();
    }

    /**
     * Creates or updates a source for a subject.It creates and assigns a source of a for a
     * dynamicallyRegister-able sourceType. Currently, it is
     * allowed to create only once source of a dynamicallyRegistrable sourceType per subject.
     * Otherwise finds the matching source and updates meta-data
     */
    @Transactional
    public MinimalSourceDetailsDTO assignOrUpdateSource(Subject subject, SourceType sourceType, Project project,
        MinimalSourceDetailsDTO sourceRegistrationDTO) throws URISyntaxException {
        Source assignedSource = null;

        List<Source> sources = subjectRepository
            .findSubjectSourcesBySourceType(subject.getUser().getLogin(),
                sourceType.getProducer(),
                sourceType.getModel(), sourceType.getCatalogVersion());

        // update meta-data for existing sources
        if(sourceRegistrationDTO.getSourceId()!=null) {
            // for manually registered devices only add meta-data
            Optional<Source> sourceToUpdate = subjectRepository.findSubjectSourcesBySourceId(subject.getUser().getLogin(), sourceRegistrationDTO.getSourceId());
            if (sourceToUpdate.isPresent()) {
                Source source = sourceToUpdate.get();
                if(sourceRegistrationDTO.getSourceName()!=null) {
                    source.setSourceName(sourceRegistrationDTO.getSourceName());
                }
                source.getAttributes().putAll(sourceRegistrationDTO.getAttributes());
                sourceRepository.save(source);
                assignedSource = source;
            } else {
                log.error("Cannot find a Source of sourceId "
                    + "already registered for subject login");
                Map<String, String> errorParams = new HashMap<>();
                errorParams.put("message",
                    "Cannot find a Source of sourceId "
                        + "already registered for subject login");
                errorParams.put("sourceId", sourceRegistrationDTO.getSourceId().toString());
                throw new CustomNotFoundException("Conflict", errorParams);
            }
        }
        else if (sourceType.getCanRegisterDynamically()) {
            // create a source and register meta data
            // we allow only one source of a source-type per subject
            if (sources.isEmpty()) {
                Source source1 = new Source()
                        .project(project)
                        .assigned(true)
                        .sourceType(sourceType);
                source1.getAttributes().putAll(sourceRegistrationDTO.getAttributes());
                // if source name is provided update source name
                if (Objects.nonNull(sourceRegistrationDTO.getSourceName())) {
                    // append the auto generated source-name to given source-name to avoid conflicts
                    source1.setSourceName(sourceRegistrationDTO.getSourceName()+"_"+source1.getSourceName());
                }

                Optional<Source> sourceToUpdate = sourceRepository.findOneBySourceName(source1.getSourceName());
                if(sourceToUpdate.isPresent()) {
                    log.error("Cannot create a source with existing source-name {}" , source1.getSourceName());
                    Map<String, String> errorParams = new HashMap<>();
                    errorParams.put("message",
                        "SourceName already in use. Cannot create a source with source-name ");
                    errorParams.put("source-name", source1.getSourceName());
                    throw new CustomNotFoundException("Conflict", errorParams);
                }
                source1 = sourceRepository.save(source1);

                assignedSource = source1;
                subject.getSources().add(source1);
            } else {
                log.error("A Source of SourceType with the specified producer and model "
                    + "already registered for subject login");
                Map<String, String> errorParams = new HashMap<>();
                errorParams
                    .put("message", "A Source of SourceType with the specified producer and model "
                        + "already registered for subject login");
                errorParams.put("producer", sourceType.getProducer());
                errorParams.put("model", sourceType.getModel());
                errorParams.put("subject-id", subject.getUser().getLogin());
                throw new CustomConflictException("Conflict", errorParams, new URI(HeaderUtil
                        .buildPath("api", "subjects", subject.getUser().getLogin(), "sources")));
            }
        }

        if (assignedSource == null) {
            log.error("Cannot find assigned source with sourceId or a source of sourceType"
                + " with the specified producer and model "
                + " is already registered for subject login ");
            Map<String, String> errorParams = new HashMap<>();
            errorParams
                .put("message", "Cannot find assigned source with sourceId or a source of sourceType"
                    + " with the specified producer and model "
                    + " is already registered for subject login ");
            errorParams.put("producer", sourceType.getProducer());
            errorParams.put("model", sourceType.getModel());
            errorParams.put("subject-id", subject.getUser().getLogin());
            errorParams.put("sourceId", sourceRegistrationDTO.getSourceId().toString());
            throw new CustomParameterizedException("InvalidRequest" , errorParams);
        }
        subjectRepository.save(subject);
        return sourceMapper.sourceToMinimalSourceDetailsDTO(assignedSource);
    }

    /**
     * Gets all sources assigned to the subject identified by :login
     * @param subject
     * @return list of sources
     */
    public List<MinimalSourceDetailsDTO> getSources(Subject subject) {
        List<Source> sources = subjectRepository.findSourcesBySubjectLogin(subject.getUser().getLogin());

        return sourceMapper.sourcesToMinimalSourceDetailsDTOs(sources);
    }

    public void deleteSubject(String login) {
        subjectRepository.findOneWithEagerBySubjectLogin(login).ifPresent(subject -> {
            unassignAllSources(subject);
            subjectRepository.delete(subject);
                log.debug("Deleted Subject: {}", subject);
            });
    }
}
