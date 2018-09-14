package org.radarcns.management.service;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarcns.management.service.dto.ProjectDTO.PRIVACY_POLICY_URL;
import static org.radarcns.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.radarcns.management.web.rest.errors.EntityName.SOURCE_TYPE;
import static org.radarcns.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_SOURCE_NOT_FOUND;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_SUBJECT_NOT_FOUND;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hibernate.envers.query.AuditEntity;
import org.radarcns.management.config.ManagementPortalProperties;
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
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.errors.BadRequestException;
import org.radarcns.management.web.rest.errors.ConflictException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.radarcns.management.web.rest.errors.InvalidStateException;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revisions;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private UserRepository userRepository;

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
            subject.getSources().forEach(s -> s.assigned(true).subject(subject));
        }
        sourceRepository.save(subject.getSources());
        return subjectMapper.subjectToSubjectDTO(subjectRepository.save(subject));
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
     * @param newSubjectDto the new subject information
     * @return the updated subject
     */
    @Transactional
    public SubjectDTO updateSubject(SubjectDTO newSubjectDto) {
        if (newSubjectDto.getId() == null) {
            return createSubject(newSubjectDto);
        }
        Subject subjectFromDb = subjectRepository.findOne(newSubjectDto.getId());
        //reset all the sources assigned to a subject to unassigned
        Set<Source> sourcesToUpdate = subjectFromDb.getSources();
        sourcesToUpdate.forEach(s -> s.subject(null).assigned(false));
        //set only the devices assigned to a subject as assigned
        subjectMapper.safeUpdateSubjectFromDTO(newSubjectDto, subjectFromDb);
        sourcesToUpdate.addAll(subjectFromDb.getSources());
        subjectFromDb.getSources().forEach(s -> s.subject(subjectFromDb).assigned(true));
        sourceRepository.save(sourcesToUpdate);
        // update participant role
        subjectFromDb.getUser().setRoles(updateParticipantRoles(subjectFromDb, newSubjectDto));
        return subjectMapper.subjectToSubjectDTO(subjectRepository.save(subjectFromDb));
    }

    private Set<Role> updateParticipantRoles(Subject subject, SubjectDTO subjectDto) {
        Set<Role> managedRoles = subject.getUser().getRoles().stream()
                // make participant inactive in projects that do not match the new project
                .map(role -> PARTICIPANT.equals(role.getAuthority().getName())
                            && !role.getProject().getProjectName().equals(
                                    subjectDto.getProject().getProjectName())
                            ? getProjectParticipantRole(role.getProject(), INACTIVE_PARTICIPANT)
                            : role)
                .collect(Collectors.toSet());
        // add participant role for current project, if the project did not change, then the set
        // will not change since the role being added here already exists in the set
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
            source.setSubject(null);
            sourceRepository.save(source);
        });
        subject.getSources().clear();
    }

    /**
     * Creates or updates a source for a subject. It creates and assigns a source of a for a
     * dynamicallyRegister-able sourceType. Currently, it is allowed to create only once source of a
     * dynamicallyRegistrable sourceType per subject. Otherwise finds the matching source and
     * updates meta-data.
     */
    @Transactional
    public MinimalSourceDetailsDTO assignOrUpdateSource(Subject subject, SourceType sourceType,
            Project project, MinimalSourceDetailsDTO sourceRegistrationDto) {
        Source assignedSource = null;

        if (sourceRegistrationDto.getSourceId() != null) {
            // update meta-data and source-name for existing sources
            assignedSource = updateSourceAssignedSubject(subject, sourceRegistrationDto);

        } else if (sourceType.getCanRegisterDynamically()) {
            List<Source> sources = subjectRepository
                    .findSubjectSourcesBySourceType(subject.getUser().getLogin(),
                    sourceType.getProducer(), sourceType.getModel(),
                    sourceType.getCatalogVersion());
            // create a source and register meta data
            // we allow only one source of a source-type per subject
            if (sources.isEmpty()) {
                Source source = new Source(sourceType)
                        .project(project)
                        .assigned(true)
                        .sourceType(sourceType)
                        .subject(subject);
                source.getAttributes().putAll(sourceRegistrationDto.getAttributes());
                // if source name is provided update source name
                if (sourceRegistrationDto.getSourceName() != null) {
                    // append the auto generated source-name to given source-name to avoid conflicts
                    source.setSourceName(
                            sourceRegistrationDto.getSourceName() + "_" + source.getSourceName());
                }
                // make sure there is no source available on the same name.
                if (sourceRepository.findOneBySourceName(source.getSourceName()).isPresent()) {
                    throw new ConflictException("SourceName already in use. Cannot create a "
                        + "source with existing source-name ", SUBJECT,
                        ErrorConstants.ERR_SOURCE_NAME_EXISTS,
                        Collections.singletonMap("source-name", source.getSourceName()));
                }
                source = sourceRepository.save(source);

                assignedSource = source;
                subject.getSources().add(source);
            } else {
                Map<String, String> errorParams = new HashMap<>();
                errorParams.put("producer", sourceType.getProducer());
                errorParams.put("model", sourceType.getModel());
                errorParams.put("catalogVersion", sourceType.getCatalogVersion());
                errorParams.put("subject-id", subject.getUser().getLogin());
                throw new ConflictException(
                    "A Source of SourceType with the specified producer, model and version"
                        + " was already registered for subject login",
                    SUBJECT, ErrorConstants.ERR_SOURCE_TYPE_EXISTS, errorParams);
            }
        } else {
            // new source since sourceId == null, but canRegisterDynamically == false
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("producer", sourceType.getProducer());
            errorParams.put("model", sourceType.getModel());
            errorParams.put("catalogVersion", sourceType.getCatalogVersion());
            errorParams.put("subject-id", subject.getUser().getLogin());
            throw new BadRequestException("The source type is not eligible for dynamic "
                    + "registration", SOURCE_TYPE, "error.InvalidDynamicSourceRegistration",
                    errorParams);
        }

        subjectRepository.save(subject);
        return sourceMapper.sourceToMinimalSourceDetailsDTO(assignedSource);
    }

    /**
     * Updates source name and attributes of the source assigned to subject. Otherwise returns
     * {@link NotFoundException}.
     * @param subject subject
     * @param sourceRegistrationDto details of source which need to be updated.
     * @return Updated {@link Source} instance.
     */
    private Source updateSourceAssignedSubject(Subject subject,
            MinimalSourceDetailsDTO sourceRegistrationDto) {
        // for manually registered devices only add meta-data
        Optional<Source> sourceToUpdate = subjectRepository.findSubjectSourcesBySourceId(
                subject.getUser().getLogin(), sourceRegistrationDto.getSourceId());

        if (sourceToUpdate.isPresent()) {
            Source source = sourceToUpdate.get();
            if (sourceRegistrationDto.getSourceName() != null) {
                source.setSourceName(sourceRegistrationDto.getSourceName());
            }
            source.getAttributes().putAll(sourceRegistrationDto.getAttributes());
            source.setAssigned(true);
            source.setSubject(subject);

            return sourceRepository.save(source);
        } else {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("sourceId", sourceRegistrationDto.getSourceId().toString());
            errorParams.put("subject-login", subject.getUser().getLogin());
            throw new NotFoundException( "No source with source-id to assigned to the subject"
                + " with subject-login", SUBJECT, ERR_SOURCE_NOT_FOUND, errorParams);
        }
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

    /**
     * Finds all sources of subject including inactive sources.
     *
     * @param subject of whom the sources should be retrieved.
     * @return list of {@link MinimalSourceDetailsDTO} of sources.
     */
    public List<MinimalSourceDetailsDTO> findSubjectSourcesFromRevisions(Subject subject) {

        Revisions<Integer, Subject> revisions = subjectRepository.findRevisions(subject.getId());
        // collect distinct sources in a set
        Set<Source> sources = revisions
                .getContent().stream().flatMap(p -> p.getEntity().getSources().stream())
                .filter(distinctByKey(Source::getSourceId))
                .collect(Collectors.toSet());
        return sources.stream().map(p -> sourceMapper.sourceToMinimalSourceDetailsDTO(p))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific revision for a given subject.
     *
     * @param login the login of the subject
     * @param revision the revision number
     * @return the subject at the given revision
     * @throws NotFoundException if there was no subject with the given login at the given
     *         revision number
     */
    public SubjectDTO findRevision(String login, Integer revision) throws NotFoundException {
        // first get latest known version of the subject, if it's deleted we can't load the entity
        // directly by e.g. findOneByLogin
        SubjectDTO latest = getLatestRevision(login);
        Subject sub = revisionService.findRevision(revision, latest.getId(), Subject.class);
        if (sub == null) {
            throw new NotFoundException("subject not found for given login and revision.", SUBJECT,
                ERR_SUBJECT_NOT_FOUND, Collections.singletonMap("subjectLogin", login));
        }
        return subjectMapper.subjectToSubjectDTO(sub);
    }

    /**
     * Get latest known revision of a subject with the given login.
     *
     * @param login the login of the subject
     * @return the latest revision for that subject
     * @throws NotFoundException if no subject was found with the given login
     */
    public SubjectDTO getLatestRevision(String login) throws NotFoundException {
        UserDTO user = (UserDTO) revisionService.getLatestRevisionForEntity(User.class,
                Arrays.asList(AuditEntity.property("login").eq(login)))
                .orElseThrow(() -> new NotFoundException("Subject latest revision not found "
                    + "for login" , SUBJECT, ERR_SUBJECT_NOT_FOUND,
                        Collections.singletonMap("subjectLogin", login)));
        return (SubjectDTO) revisionService.getLatestRevisionForEntity(Subject.class,
                Arrays.asList(AuditEntity.property("user").eq(user)))
                .orElseThrow(() -> new NotFoundException("Subject latest revision not found "
                    + "for login" , SUBJECT, ERR_SUBJECT_NOT_FOUND,
                    Collections.singletonMap("subjectLogin", login)));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }


    /**
     * Finds {@link Subject} from databased from login provided.
     * @param login of subject to look for.
     * @return {@link Subject} loaded.
     */
    public Subject findOneByLogin(String login) {
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        return subject.orElseThrow(() ->
            new NotFoundException("Subject not found with login", SUBJECT,
                ERR_SUBJECT_NOT_FOUND)
        );
    }

    /**
     * Gets relevant privacy-policy-url for this subject.
     * <p>
     *     If the active project of the subject has a valid privacy-policy-url returns that url.
     *     Otherwise, it loads the default URL from ManagementPortal configurations that is
     *     general.
     * </p>
     * @param subject to get relevant policy url
     * @return URL of privacy policy for this token
     */
    protected URL getPrivacyPolicyUrl(Subject subject) {

        // load default url from config
        String policyUrl = subject.getActiveProject()
                .map(p -> p.getAttributes().get(PRIVACY_POLICY_URL))
                .filter(u -> u != null && !u.isEmpty())
                .orElse(managementPortalProperties.getCommon().getPrivacyPolicyUrl());

        try {
            return new URL(policyUrl);
        } catch (MalformedURLException e) {
            Map<String, String> params = new HashMap<>();
            params.put("url" , policyUrl);
            params.put("message" , e.getMessage());
            throw new InvalidStateException("No valid privacy-policy Url configured. Please "
                    + "verify your project's privacy-policy url and/or general url config",
                    OAUTH_CLIENT, ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED,
                    params);
        }
    }

    /**
     * Not activated users should be automatically deleted after 3 days. <p> This is scheduled to
     * get fired everyday, at midnight. Preferably we do this scan before
     * {@link UserService#removeNotActivatedUsers()}, since this will remove the not activated
     * user tied to the subject as well.</p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeNotActivatedSubjects() {
        log.info("Scheduled scan for expired subject accounts starting now");
        ZonedDateTime cutoff = ZonedDateTime.now().minus(Period.ofDays(3));

        // first delete non-activated users related to subjects
        userRepository.findAllByActivated(false).stream()
                .filter(user -> revisionService.getAuditInfo(user).getCreatedAt().isBefore(cutoff))
                .map(User::getLogin)
                .map(subjectRepository::findOneWithEagerBySubjectLogin)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(subject -> {
                    log.info("Deleting not activated subject after 3 days: {}", subject);
                    subjectRepository.delete(subject);
                });
    }
}
