package org.radarcns.management.service;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;

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
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    /**
     * Create a new subject.
     *
     * @param subjectDto the subject information
     * @return the newly created subject
     */
    @Transactional
    public SubjectDTO createSubject(SubjectDTO subjectDto) {
        Subject subject = subjectMapper.subjectDTOToSubject(subjectDto);
        //assign roles
        User user = subject.getUser();
        user.getRoles().add(getProjectParticipantRole(
                projectMapper.projectDTOToProject(subjectDto.getProject()), PARTICIPANT));

        // set password and reset keys
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        // setting default language key to "en", required to set email context, Find a workaround
        user.setLangKey("en");
        user.setResetDate(ZonedDateTime.now());
        // default subject is activated.
        user.setActivated(true);
        //set if any devices are set as assigned
        if (subject.getSources() != null && !subject.getSources().isEmpty()) {
            for (Source source : subject.getSources()) {
                source.setAssigned(true);
            }
        }
        subject = subjectRepository.save(subject);
        return subjectMapper.subjectToSubjectDTO(subject);
    }

    /**
     * Fetch Participant role of the project if available, otherwise create a new Role and assign.
     *
     * @param project project subject is assigned to
     * @return relevant Participant role
     * @throws java.util.NoSuchElementException if the authority name is not in the database
     */
    private Role getProjectParticipantRole(Project project, String authority) {
        return roleRepository.findOneByProjectIdAndAuthorityName(project.getId(), authority)
                .orElseGet(() -> {
                    Role subjectRole = new Role();
                    // If we do not have the participant authority something is very wrong, and the
                    // .get() will trigger a NoSuchElementException, which will be translated
                    // into a 500 response.
                    subjectRole.setAuthority(authorityRepository.findByAuthorityName(
                            authority).get());
                    subjectRole.setProject(project);
                    roleRepository.save(subjectRole);
                    return subjectRole;
                });
    }


    /**
     * Update a subject's information.
     *
     * @param subjectDto the new subject information
     * @return the updated subject
     */
    @Transactional
    public SubjectDTO updateSubject(SubjectDTO subjectDto) {
        if (subjectDto.getId() == null) {
            return createSubject(subjectDto);
        }
        Subject subject = subjectRepository.findOne(subjectDto.getId());
        //reset all the sources assigned to a subject to unassigned
        for (Source source : subject.getSources()) {
            source.setAssigned(false);
            sourceRepository.save(source);
        }
        //set only the devices assigned to a subject as assigned
        subjectMapper.safeUpdateSubjectFromDTO(subjectDto, subject);
        for (Source source : subject.getSources()) {
            source.setAssigned(true);
        }
        // update participant role
        Set<Role> managedRoles = updateParticipantRoles(subject, subjectDto);
        subject.getUser().setRoles(managedRoles);
        subject = subjectRepository.save(subject);

        return subjectMapper.subjectToSubjectDTO(subject);
    }

    private Set<Role> updateParticipantRoles(Subject subject, SubjectDTO subjectDto) {
        Set<Role> managedRoles = subject.getUser().getRoles().stream().map(role -> {
            // inactivate existing patient roles
            if (PARTICIPANT.equals(role.getAuthority().getName())) {
                return getProjectParticipantRole(role.getProject(), INACTIVE_PARTICIPANT);
            } else {
                return role;
            }
            // and remove role for current project
        }).filter(r -> !r.getProject().getProjectName().equals(subjectDto.getProject()
                .getProjectName())).collect(Collectors.toSet());
        // add participant role for current project
        managedRoles.add(getProjectParticipantRole(projectMapper.projectDTOToProject(subjectDto
                .getProject()), PARTICIPANT));
        return managedRoles;
    }


    /**
     * Get a page of subjects.
     *
     * @param pageable the page information
     * @return the requested page of subjects
     */
    public Page<SubjectDTO> findAll(Pageable pageable) {
        return subjectRepository.findAllWithEagerRelationships(pageable)
                .map(subjectMapper::subjectToSubjectDTO);
    }

    /**
     * Discontinue the given subject.
     *
     * <p>A discontinued subject is not deleted from the database, but will be prevented from
     * logging into the system, sending data, or otherwise interacting with the system.</p>
     *
     * @param subjectDto the subject to discontinue
     * @return the discontinued subject
     */
    public SubjectDTO discontinueSubject(SubjectDTO subjectDto) {
        Subject subject = subjectRepository.findOne(subjectDto.getId());
        // reset all the sources assigned to a subject to unassigned
        unassignAllSources(subject);

        // set the removed flag and deactivate the user to prevent them from refreshing their
        // access token
        subject.setRemoved(true);
        subject.getUser().setActivated(false);
        return subjectMapper.subjectToSubjectDTO(subjectRepository.save(subject));
    }

    /**
     * Unassign all sources from a subject. This method saves the unassigned sources, but does NOT
     * save the subject in question. This is the responsibility of the caller.
     *
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
     * dynamicallyRegister-able sourceType. Currently, it is allowed to create only once source of a
     * dynamicallyRegistrable sourceType per subject. Otherwise finds the matching source and
     * updates meta-data.
     */
    @Transactional
    public MinimalSourceDetailsDTO assignOrUpdateSource(Subject subject, SourceType sourceType,
            Project project, MinimalSourceDetailsDTO sourceRegistrationDto)
            throws URISyntaxException {
        Source assignedSource = null;

        List<Source> sources = subjectRepository
                .findSubjectSourcesBySourceType(subject.getUser().getLogin(),
                        sourceType.getProducer(), sourceType.getModel(),
                        sourceType.getCatalogVersion());

        // update meta-data for existing sources
        if (sourceRegistrationDto.getSourceId() != null) {
            // for manually registered devices only add meta-data
            Optional<Source> sourceToUpdate = subjectRepository.findSubjectSourcesBySourceId(
                    subject.getUser().getLogin(), sourceRegistrationDto.getSourceId());
            if (sourceToUpdate.isPresent()) {
                Source source = sourceToUpdate.get();
                if (sourceRegistrationDto.getSourceName() != null) {
                    source.setSourceName(sourceRegistrationDto.getSourceName());
                }
                source.getAttributes().putAll(sourceRegistrationDto.getAttributes());
                sourceRepository.save(source);
                assignedSource = source;
            } else {
                log.error("Cannot find a Source of sourceId already registered for subject login");
                Map<String, String> errorParams = new HashMap<>();
                errorParams.put("message",
                        "Cannot find a Source of sourceId already registered for subject login");
                errorParams.put("sourceId", sourceRegistrationDto.getSourceId().toString());
                throw new CustomNotFoundException(ErrorConstants.ERR_SOURCE_NOT_FOUND, errorParams);
            }
        } else if (sourceType.getCanRegisterDynamically()) {
            // create a source and register meta data
            // we allow only one source of a source-type per subject
            if (sources.isEmpty()) {
                Source source1 = new Source()
                        .project(project)
                        .assigned(true)
                        .sourceType(sourceType);
                source1.getAttributes().putAll(sourceRegistrationDto.getAttributes());
                // if source name is provided update source name
                if (Objects.nonNull(sourceRegistrationDto.getSourceName())) {
                    // append the auto generated source-name to given source-name to avoid conflicts
                    source1.setSourceName(sourceRegistrationDto.getSourceName() + "_"
                            + source1.getSourceName());
                }

                Optional<Source> sourceToUpdate = sourceRepository.findOneBySourceName(
                        source1.getSourceName());
                if (sourceToUpdate.isPresent()) {
                    log.error("Cannot create a source with existing source-name {}",
                            source1.getSourceName());
                    Map<String, String> errorParams = new HashMap<>();
                    errorParams.put("message",
                            "SourceName already in use. Cannot create a source with source-name ");
                    errorParams.put("source-name", source1.getSourceName());
                    throw new CustomNotFoundException(ErrorConstants.ERR_SOURCE_NAME_EXISTS,
                            errorParams);
                }
                source1 = sourceRepository.save(source1);

                assignedSource = source1;
                subject.getSources().add(source1);
            } else {
                log.error("A Source of SourceType with the specified producer, model and version "
                        + "was already registered for subject login");
                Map<String, String> errorParams = new HashMap<>();
                errorParams.put("message", "A Source of SourceType with the specified producer, "
                        + "model and version was already registered for subject login");
                errorParams.put("producer", sourceType.getProducer());
                errorParams.put("model", sourceType.getModel());
                errorParams.put("catalogVersion", sourceType.getCatalogVersion());
                errorParams.put("subject-id", subject.getUser().getLogin());
                throw new CustomConflictException(ErrorConstants.ERR_SOURCE_TYPE_EXISTS,
                        errorParams, new URI(HeaderUtil.buildPath("api", "subjects",
                        subject.getUser().getLogin(), "sources")));
            }
        }

        /** all of the above codepaths lead to an initialized assignedSource or throw an
         /* exception, so probably we can safely remove this check.
         */
        if (assignedSource == null) {
            log.error("Cannot find assigned source with sourceId or a source of sourceType with "
                    + "the specified producer and model is already registered for subject login ");
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Cannot find assigned source with sourceId or a source of "
                    + "sourceType with the specified producer and model is already registered "
                    + "for subject login ");
            errorParams.put("producer", sourceType.getProducer());
            errorParams.put("model", sourceType.getModel());
            errorParams.put("subject-id", subject.getUser().getLogin());
            errorParams.put("sourceId", sourceRegistrationDto.getSourceId().toString());
            throw new CustomParameterizedException("InvalidRequest", errorParams);
        }
        subjectRepository.save(subject);
        return sourceMapper.sourceToMinimalSourceDetailsDTO(assignedSource);
    }

    /**
     * Gets all sources assigned to the subject identified by :login.
     *
     * @return list of sources
     */
    public List<MinimalSourceDetailsDTO> getSources(Subject subject) {
        List<Source> sources = subjectRepository.findSourcesBySubjectLogin(subject.getUser()
                .getLogin());

        return sourceMapper.sourcesToMinimalSourceDetailsDTOs(sources);
    }

    /**
     * Delete the subject with the given login from the database.
     *
     * @param login the login
     */
    public void deleteSubject(String login) {
        subjectRepository.findOneWithEagerBySubjectLogin(login).ifPresent(subject -> {
            unassignAllSources(subject);
            subjectRepository.delete(subject);
            log.debug("Deleted Subject: {}", subject);
        });
    }
}
