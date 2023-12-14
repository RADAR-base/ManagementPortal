package org.radarbase.management.service;

import org.hibernate.envers.query.AuditEntity;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.Group;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.Source;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.AuthorityRepository;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.RoleRepository;
import org.radarbase.management.repository.SourceRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.repository.filters.SubjectSpecification;
import org.radarbase.management.security.NotAuthorizedException;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.service.mapper.SourceMapper;
import org.radarbase.management.service.mapper.SubjectMapper;
import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.InvalidStateException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
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
import java.util.stream.Stream;

import static org.radarbase.auth.authorization.Permission.SUBJECT_READ;
import static org.radarbase.auth.authorization.RoleAuthority.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.RoleAuthority.PARTICIPANT;
import static org.radarbase.management.service.dto.ProjectDTO.PRIVACY_POLICY_URL;
import static org.radarbase.management.web.rest.errors.EntityName.GROUP;
import static org.radarbase.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_TYPE;
import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_GROUP_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SOURCE_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SUBJECT_NOT_FOUND;

/**
 * Created by nivethika on 26-5-17.
 */
@Service
@Transactional
public class SubjectService {

    private static final Logger log = LoggerFactory.getLogger(SubjectService.class);

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuthService authService;

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
        Project project = projectMapper.projectDTOToProject(subjectDto.getProject());
        Role projectParticipantRole = getProjectParticipantRole(project, PARTICIPANT);
        Set<Role> roles = user.getRoles();
        roles.add(projectParticipantRole);

        // Set group
        subject.setGroup(getSubjectGroup(project, subjectDto.getGroup()));

        // set password and reset keys
        user.setPassword(passwordService.generateEncodedPassword());
        user.setResetKey(passwordService.generateResetKey());
        // setting default language key to "en", required to set email context, Find a workaround
        user.setLangKey("en");
        user.setResetDate(ZonedDateTime.now());
        // default subject is activated.
        user.setActivated(true);
        //set if any devices are set as assigned
        if (subject.getSources() != null && !subject.getSources().isEmpty()) {
            subject.getSources().forEach(s -> s.assigned(true).subject(subject));
        }
        if (subject.getEnrollmentDate() == null) {
            subject.setEnrollmentDate(ZonedDateTime.now());
        }
        sourceRepository.saveAll(subject.getSources());
        return subjectMapper.subjectToSubjectReducedProjectDTO(subjectRepository.save(subject));
    }

    private Group getSubjectGroup(Project project, String groupName) {
        if (project == null || groupName == null) {
            return null;
        }
        return groupRepository.findByProjectIdAndName(project.getId(), groupName)
                .orElseThrow(() -> new BadRequestException(
                        "Group " + groupName + " does not exist in project "
                                + project.getProjectName(),
                        GROUP,
                        ERR_GROUP_NOT_FOUND)
                );
    }

    /**
     * Fetch Participant role of the project if available, otherwise create a new Role and assign.
     *
     * @param project project subject is assigned to
     * @return relevant Participant role
     * @throws java.util.NoSuchElementException if the authority name is not in the database
     */
    private Role getProjectParticipantRole(Project project, RoleAuthority authority) {
        return roleRepository.findOneByProjectIdAndAuthorityName(project.getId(),
                        authority.getAuthority())
                .orElseGet(() -> {
                    Role subjectRole = new Role();
                    Authority auth = authorityRepository.findByAuthorityName(
                            authority.getAuthority())
                            .orElseGet(() -> authorityRepository.save(new Authority(authority)));
                    subjectRole.setAuthority(auth);
                    subjectRole.setProject(project);
                    return roleRepository.save(subjectRole);
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
        Subject subjectFromDb = ensureSubject(newSubjectDto);
        Set<Source> sourcesToUpdate = subjectFromDb.getSources();
        //set only the devices assigned to a subject as assigned
        subjectMapper.safeUpdateSubjectFromDTO(newSubjectDto, subjectFromDb);
        sourcesToUpdate.addAll(subjectFromDb.getSources());
        subjectFromDb.getSources()
                .forEach(s -> s.subject(subjectFromDb).assigned(true));
        sourceRepository.saveAll(sourcesToUpdate);
        // update participant role
        subjectFromDb.getUser().setRoles(updateParticipantRoles(subjectFromDb, newSubjectDto));
        // Set group
        subjectFromDb.setGroup(getSubjectGroup(
                subjectFromDb.getActiveProject().orElse(null),
                newSubjectDto.getGroup()));
        return subjectMapper.subjectToSubjectReducedProjectDTO(
                subjectRepository.save(subjectFromDb));
    }

    private Set<Role> updateParticipantRoles(Subject subject, SubjectDTO subjectDto) {
        if (subjectDto.getProject() == null || subjectDto.getProject().getProjectName() == null) {
            return subject.getUser().getRoles();
        }

        Stream<Role> existingRoles = subject.getUser().getRoles().stream()
                .map(role -> {
                    // make participant inactive in projects that do not match the new project
                    if (role.getAuthority().getName().equals(PARTICIPANT.getAuthority())
                            && !role.getProject().getProjectName().equals(
                                    subjectDto.getProject().getProjectName())) {
                        return getProjectParticipantRole(role.getProject(), INACTIVE_PARTICIPANT);
                    } else {
                        // do not modify other roles.
                        return role;
                    }
                });

        // Ensure that given project is present
        Stream<Role> newProjectRole = Stream.of(getProjectParticipantRole(
                projectMapper.projectDTOToProject(subjectDto.getProject()), PARTICIPANT));

        return Stream.concat(existingRoles, newProjectRole)
                .collect(Collectors.toSet());
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
        Subject subject = ensureSubject(subjectDto);
        // reset all the sources assigned to a subject to unassigned
        unassignAllSources(subject);

        // set the removed flag and deactivate the user to prevent them from refreshing their
        // access token
        subject.setRemoved(true);
        subject.getUser().setActivated(false);
        return subjectMapper.subjectToSubjectReducedProjectDTO(subjectRepository.save(subject));
    }

    private Subject ensureSubject(SubjectDTO subjectDto) {
        return subjectRepository.findById(subjectDto.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Subject with ID " + subjectDto.getId() + " not found.",
                        SUBJECT, ERR_SUBJECT_NOT_FOUND));
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
            source.setDeleted(true);
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
        Source assignedSource;

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
                throw new ConflictException(
                    "A Source of SourceType with the specified producer, model and version"
                        + " was already registered for subject login",
                    SUBJECT, ErrorConstants.ERR_SOURCE_TYPE_EXISTS,
                        sourceTypeAttributes(sourceType, subject));
            }
        } else {
            // new source since sourceId == null, but canRegisterDynamically == false
            throw new BadRequestException("The source type is not eligible for dynamic "
                    + "registration", SOURCE_TYPE, "error.InvalidDynamicSourceRegistration",
                    sourceTypeAttributes(sourceType, subject));
        }

        subjectRepository.save(subject);
        return sourceMapper.sourceToMinimalSourceDetailsDTO(assignedSource);
    }

    private static Map<String, String> sourceTypeAttributes(SourceType sourceType,
            Subject subject) {
        Map<String, String> errorParams = new HashMap<>();
        errorParams.put("producer", sourceType.getProducer());
        errorParams.put("model", sourceType.getModel());
        errorParams.put("catalogVersion", sourceType.getCatalogVersion());
        errorParams.put("userId", subject.getUser().getLogin());
        return errorParams;
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
        Source source = subjectRepository.findSubjectSourcesBySourceId(
                subject.getUser().getLogin(), sourceRegistrationDto.getSourceId())
                .orElseThrow(() -> {
                    Map<String, String> errorParams = new HashMap<>();
                    errorParams.put("sourceId", sourceRegistrationDto.getSourceId().toString());
                    errorParams.put("subject-login", subject.getUser().getLogin());
                    return new NotFoundException( "No source with source-id to assigned to the "
                            + "subject with subject-login", SUBJECT, ERR_SOURCE_NOT_FOUND,
                            errorParams);
                });

        if (sourceRegistrationDto.getSourceName() != null) {
            source.setSourceName(sourceRegistrationDto.getSourceName());
        }
        source.getAttributes().putAll(sourceRegistrationDto.getAttributes());
        source.setAssigned(true);
        source.setSubject(subject);

        return sourceRepository.save(source);
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
                .toList();
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
    public SubjectDTO findRevision(String login, Integer revision)
            throws NotFoundException, NotAuthorizedException {
        // first get latest known version of the subject, if it's deleted we can't load the entity
        // directly by e.g. findOneByLogin
        SubjectDTO latest = getLatestRevision(login);
        authService.checkPermission(SUBJECT_READ, e -> e
                .project(latest.getProject().getProjectName())
                .subject(latest.getLogin()));
        SubjectDTO sub = revisionService
                .findRevision(revision, latest.getId(), Subject.class,
                        subjectMapper::subjectToSubjectReducedProjectDTO);

        if (sub == null) {
            throw new NotFoundException("subject not found for given login and revision.", SUBJECT,
                ERR_SUBJECT_NOT_FOUND, Collections.singletonMap("subjectLogin", login));
        }
        return sub;
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
                List.of(AuditEntity.property("login").eq(login)))
                .orElseThrow(() -> new NotFoundException("Subject latest revision not found "
                    + "for login" , SUBJECT, ERR_SUBJECT_NOT_FOUND,
                        Collections.singletonMap("subjectLogin", login)));
        return (SubjectDTO) revisionService.getLatestRevisionForEntity(Subject.class,
                List.of(AuditEntity.property("user").eq(user)))
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
    @Nonnull
    public Subject findOneByLogin(String login) {
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        return subject.orElseThrow(() ->
            new NotFoundException("Subject not found with login", SUBJECT,
                ERR_SUBJECT_NOT_FOUND)
        );
    }

    /**
     * Find all subjects matching given filter.
     * @param criteria filter and sort for subjects.
     * @return page of subjects matching filter.
     */
    public Page<Subject> findAll(SubjectCriteria criteria) {
        // Pageable is required to set the page limit,
        // but the page should always be zero
        // since the lastLoadedId param defines the offset
        // within the query specification
        return subjectRepository.findAll(new SubjectSpecification(criteria),
                criteria.getPageable());
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
                .filter(u -> !u.isEmpty())
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
}
