package org.radarcns.management.service;

import org.hibernate.envers.query.AuditEntity;
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
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revisions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;

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
                source.setAssigned(true);
                source.setSubject(subject);
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
                Source source1 = new Source(sourceType)
                        .project(project)
                        .assigned(true)
                        .sourceType(sourceType)
                        .subject(subject);
                source1.getAttributes().putAll(sourceRegistrationDto.getAttributes());
                // if source name is provided update source name
                if (Objects.nonNull(sourceRegistrationDto.getSourceName())) {
                    // append the auto generated source-name to given source-name to avoid conflicts
                    source1.setSourceName(
                            sourceRegistrationDto.getSourceName() + "_" + source1.getSourceName());
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
                        errorParams, ResourceUriService.getUri(sources.get(0)));
            }
        }

        /* all of the above codepaths lead to an initialized assignedSource or throw an
         * exception, so probably we can safely remove this check.
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
     * @throws CustomNotFoundException if there was no subject with the given login at the given
     *         revision number
     */
    public SubjectDTO findRevision(String login, Integer revision) throws CustomNotFoundException {
        // first get latest known version of the subject, if it's deleted we can't load the entity
        // directly by e.g. findOneByLogin
        SubjectDTO latest = getLatestRevision(login);
        Subject sub = revisionService.findRevision(revision, latest.getId(), Subject.class);
        if (sub == null) {
            throw new CustomNotFoundException(ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                    Collections.singletonMap("subjectLogin", login));
        }
        return subjectMapper.subjectToSubjectDTO(sub);
    }

    /**
     * Get latest known revision of a subject with the given login.
     *
     * @param login the login of the subject
     * @return the latest revision for that subject
     * @throws CustomNotFoundException if no subject was found with the given login
     */
    public SubjectDTO getLatestRevision(String login) throws CustomNotFoundException {
        UserDTO user = (UserDTO) revisionService.getLatestRevisionForEntity(User.class,
                Arrays.asList(AuditEntity.property("login").eq(login)))
                .orElseThrow(() -> new CustomNotFoundException(ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                        Collections.singletonMap("subjectLogin", login)));
        return (SubjectDTO) revisionService.getLatestRevisionForEntity(Subject.class,
                Arrays.asList(AuditEntity.property("user").eq(user)))
                .orElseThrow(() -> new CustomNotFoundException(ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                        Collections.singletonMap("subjectLogin", login)));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
