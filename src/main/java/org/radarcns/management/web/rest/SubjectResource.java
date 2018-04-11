package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.radarcns.auth.config.Constants;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.ResourceLocationService;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarcns.auth.authorization.Permission.SUBJECT_CREATE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_DELETE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Subject.
 */
@RestController
@RequestMapping("/api")
public class SubjectResource {

    private final Logger log = LoggerFactory.getLogger(SubjectResource.class);

    private static final String ENTITY_NAME = "subject";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private AuditEventRepository eventRepository;

    /**
     * POST  /subjects : Create a new subject.
     *
     * @param subjectDto the subjectDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subjectDto, or
     *      with status 400 (Bad Request) if the subject has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/subjects")
    @Timed
    public ResponseEntity<SubjectDTO> createSubject(@RequestBody SubjectDTO subjectDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Subject : {}", subjectDto);
        if (subjectDto.getProject() == null || subjectDto.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "projectrequired",
                            "A subject should be assigned to a project")).build();
        }
        checkPermissionOnProject(getJWT(servletRequest), SUBJECT_CREATE,
                subjectDto.getProject().getProjectName());

        if (subjectDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "idexists",
                            "A new subject cannot already have an ID")).build();
        }
        if (subjectDto.getLogin() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "loginrequired",
                            "A subject login is required"))
                    .build();
        }
        if (subjectDto.getExternalId() != null && !subjectDto.getExternalId().isEmpty()
                && subjectRepository.findOneByProjectNameAndExternalId(subjectDto.getProject()
                .getProjectName(), subjectDto.getExternalId()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "subjectExists",
                            "A subject with given project-id and external-id already exists"))
                    .build();
        }

        SubjectDTO result = subjectService.createSubject(subjectDto);
        return ResponseEntity.created(ResourceLocationService.getLocation(subjectDto))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getLogin()))
                .body(result);
    }

    /**
     * PUT  /subjects : Updates an existing subject.
     *
     * @param subjectDto the subjectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDto, or with
     *      status 400 (Bad Request) if the subjectDto is not valid, or with status 500 (Internal
     *      Server Error) if the subjectDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects")
    @Timed
    public ResponseEntity<SubjectDTO> updateSubject(@RequestBody SubjectDTO subjectDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Subject : {}", subjectDto);
        if (subjectDto.getProject() == null || subjectDto.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "projectrequired",
                            "A subject should be assigned to a project")).body(null);
        }
        if (subjectDto.getId() == null) {
            return createSubject(subjectDto);
        }
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDto.getProject().getProjectName(), subjectDto.getLogin());
        SubjectDTO result = subjectService.updateSubject(subjectDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDto.getLogin()))
                .body(result);
    }

    /**
     * PUT  /subjects/discontinue : Discontinue a subject. A discontinued subject is not allowed to
     * send data to the system anymore.
     *
     * @param subjectDto the subjectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDto, or with
     *      status 400 (Bad Request) if the subjectDto is not valid, or with status 500 (Internal
     *      Server Error) if the subjectDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects/discontinue")
    @Timed
    public ResponseEntity<SubjectDTO> discontinueSubject(@RequestBody SubjectDTO subjectDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Subject : {}", subjectDto);
        if (subjectDto.getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "subjectNotAvailable", "No subject found"))
                    .body(null);
        }

        if (subjectDto.getProject() == null || subjectDto.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "projectrequired",
                            "A subject should be assigned to a project")).body(null);
        }
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDto.getProject().getProjectName(), subjectDto.getLogin());

        // In principle this is already captured by the PostUpdate event listener, adding this
        // event just makes it more clear a subject was discontinued.
        eventRepository.add(new AuditEvent(SecurityUtils.getCurrentUserLogin(),
                "SUBJECT_DISCONTINUE", "subject_login=" + subjectDto.getLogin()));
        SubjectDTO result = subjectService.discontinueSubject(subjectDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDto.getLogin()))
                .body(result);
    }


    /**
     * GET  /subjects : get all the subjects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of subjects in body
     */
    @GetMapping("/subjects")
    @Timed
    public ResponseEntity<List<SubjectDTO>> getAllSubjects(@ApiParam Pageable pageable,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "externalId", required = false) String externalId,
            @RequestParam(value = "withInactiveParticipants", required = false)
                    Boolean withInactiveParticipantsParam)
            throws NotAuthorizedException {
        checkPermission(getJWT(servletRequest), SUBJECT_READ);
        log.debug("ProjectName {} and external {}", projectName, externalId);
        // if not specified do not include inactive patients
        boolean withInactive =
                withInactiveParticipantsParam != null ? withInactiveParticipantsParam : false;

        List<String> authoritiesToInclude = withInactive ? Arrays.asList(PARTICIPANT,
                INACTIVE_PARTICIPANT) : Collections.singletonList(PARTICIPANT);

        if (projectName != null && externalId != null) {
            Optional<Subject> subject = Optional.empty();
            subjectRepository
                    .findOneByProjectNameAndExternalIdAndAuthoritiesIn(projectName, externalId,
                            authoritiesToInclude);

            if (!subject.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject.get());
            return ResponseEntity.ok(Collections.singletonList(subjectDto));
        } else if (projectName == null && externalId != null) {
            List<Subject> subjects = subjectRepository
                    .findAllByExternalIdAndAuthoritiesIn(externalId, authoritiesToInclude);
            return ResponseUtil
                    .wrapOrNotFound(Optional.of(subjectMapper.subjectsToSubjectDTOs(subjects)));
        } else if (projectName != null) {
            Page<SubjectDTO> page = subjectRepository
                    .findAllByProjectNameAndAuthoritiesIn(pageable, projectName,
                            authoritiesToInclude).map(subjectMapper::subjectToSubjectDTO);

            HttpHeaders headers = PaginationUtil
                    .generatePaginationHttpHeaders(page, "/api/subjects");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
        log.debug("REST request to get all Subjects");
        Page<SubjectDTO> page = subjectService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/subjects");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /subjects/:login : get the "login" subject.
     *
     * @param login the login of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     *      404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to get Subject : {}", login);
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, subjectDto.getProject()
                .getProjectName(), subjectDto.getLogin());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(subjectDto));
    }

    /**
     * GET  /subjects/:login/revisions/:revisionNb : get the "login" subject at revisionNb
     * 'revisionNb'.
     *
     * @param login the login of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     *         404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}"
            + "/revisions/{revisionNb:^[0-9]*$}")
    @Timed
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable String login,
            @PathVariable Integer revisionNb) throws NotAuthorizedException {
        log.debug("REST request to get Subject : {}, for revisionNb: {}", login, revisionNb);
        SubjectDTO subjectDto = subjectService.findRevision(login, revisionNb);
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, subjectDto.getProject()
                .getProjectName(), subjectDto.getLogin());
        return ResponseEntity.ok(subjectDto);
    }

    /**
     * DELETE  /subjects/:login : delete the "login" subject.
     *
     * @param login the login of the subjectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSubject(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to delete Subject : {}", login);
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_DELETE, subjectDto.getProject()
                .getProjectName(), subjectDto.getLogin());
        subjectService.deleteSubject(login);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, login)).build();
    }

    /**
     * POST  /subjects/:login/sources: Assign a source to the specified user.
     *
     * <p>The request body is a {@link MinimalSourceDetailsDTO}. At minimum, the source should
     * define it's source type by either supplying the sourceTypeId, or the combination of
     * (sourceTypeProducer, sourceTypeModel, sourceTypeCatalogVersion) fields. A source ID will be
     * automatically generated. The source ID will be a new random UUID, and the source name, if not
     * provided, will be the device model, appended with a dash and the first eight characters of
     * the UUID. The sources will be created and assigned to the specified user.</p>
     *
     * <p>If you need to assign existing sources, simply specify either of id, sourceId, or
     * sourceName fields.</p>
     *
     * @param sourceDto The {@link MinimalSourceDetailsDTO} specification
     * @return The {@link MinimalSourceDetailsDTO} completed with all identifying fields.
     */
    @PostMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @ApiResponses({
            @ApiResponse(code = 200, message = "An existing source was assigned"),
            @ApiResponse(code = 201, message = "A new source was created and assigned"),
            @ApiResponse(code = 400, message = "You must supply either a Source Type ID, or the "
                    + "combination of (sourceTypeProducer, sourceTypeModel, catalogVersion)"),
            @ApiResponse(code = 404, message = "Either the subject or the source type was not "
                    + "found.")
    })
    @Timed
    public ResponseEntity<MinimalSourceDetailsDTO> assignSources(@PathVariable String login,
            @RequestBody MinimalSourceDetailsDTO sourceDto) throws URISyntaxException,
            NotAuthorizedException {
        // check the subject id
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Subject sub = subject.get();
        // find the PARTICIPANT role for this subject
        Optional<Role> roleOptional = sub.getUser().getRoles().stream()
                .filter(r -> r.getAuthority().getName().equals(PARTICIPANT))
                .findFirst();
        if (!roleOptional.isPresent()) {
            // no participant role found
            HashMap<String, String> params = new HashMap<>();
            params.put("message", "Supplied login is not a participant in any study, a source can"
                    + "not be assigned.");
            params.put("login", login);
            throw new CustomParameterizedException("error.loginNotParticipant", params);
        }
        Role role = roleOptional.get();
        // find out source type id of supplied source
        Long sourceTypeId = sourceDto.getSourceTypeId();
        if (sourceTypeId == null) {
            // check if combination (producer, model, version) is present
            final String msg = "error.sourceNotFound";
            try {
                String producer = Objects.requireNonNull(sourceDto.getSourceTypeProducer(), msg);
                String model = Objects.requireNonNull(sourceDto.getSourceTypeModel(), msg);
                String version = Objects.requireNonNull(sourceDto.getSourceTypeCatalogVersion(),
                        msg);
                SourceTypeDTO sourceTypeDto = sourceTypeService
                        .findByProducerAndModelAndVersion(producer, model, version);
                if (Objects.isNull(sourceTypeDto)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(
                            ENTITY_NAME, "sourceNotFound", String.join(" ", producer, model,
                                    version))).build();
                }
                sourceTypeId = sourceTypeDto.getId();
            } catch (NullPointerException ex) {
                log.error(ex.getMessage() + ", supplied sourceDto: " + sourceDto.toString());
                throw new CustomParameterizedException(ex.getMessage(),
                        Collections.singletonMap("message", "You must supply either the "
                                + "sourceTypeId, or the combination of "
                                + "(sourceTypeProducer, sourceTypeModel, catalogVersion) fields."));
            }
        }
        // find whether the relevant source-type is available in the subject's project
        Optional<SourceType> sourceType;
        sourceType = projectRepository.findSourceTypeByProjectIdAndSourceTypeId(
                role.getProject().getId(), sourceTypeId);

        if (!sourceType.isPresent()) {
            // return bad request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(HeaderUtil
                    .createAlert("sourceTypeNotAvailable",
                            "The source type is not registered in the given project")).build();
        }

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE, role.getProject()
                .getProjectName(), sub.getUser().getLogin());

        // check if any of id, sourceID, sourceName were non-null
        boolean existing = Stream.of(sourceDto.getId(), sourceDto.getSourceName(),
                sourceDto.getSourceId())
                .map(Objects::nonNull).reduce(false, (r1, r2) -> r1 || r2);

        // handle the source registration
        MinimalSourceDetailsDTO sourceRegistered = subjectService
                .assignOrUpdateSource(sub, sourceType.get(), role.getProject(), sourceDto);

        // Return the correct response type, either created if a new source was created, or ok if
        // an existing source was provided. If an existing source was given but not found, the
        // assignOrUpdateSource would throw an error and we would not reach this point.
        if (!existing) {
            return ResponseEntity.created(ResourceLocationService.getLocation(sourceRegistered))
                    .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, login))
                    .body(sourceRegistered);
        } else {
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME,
                    login)).body(sourceRegistered);
        }
    }

    /**
     * Get sources assigned to a subject.
     *
     * @param login the subject login
     * @return the sources
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @Timed
    public ResponseEntity<List<MinimalSourceDetailsDTO>> getSubjectSources(
            @PathVariable String login,
            @RequestParam(value = "withInactiveSources", required = false)
                    Boolean withInactiveSourcesParam) throws NotAuthorizedException {

        boolean withInactiveSources = withInactiveSourcesParam == null ? false :
                withInactiveSourcesParam;
        // check the subject id
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, subjectDto.getProject()
                .getProjectName(), subjectDto.getLogin());

        if (withInactiveSources) {
            return ResponseEntity.ok()
                    .body(subjectService.findSubjectSourcesFromRevisions(subject.get()));
        }

        log.debug("REST request to get sources of Subject : {}", login);

        return ResponseEntity.ok().body(subjectService.getSources(subject.get()));
    }
}
