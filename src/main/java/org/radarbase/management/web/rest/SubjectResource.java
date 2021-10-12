package org.radarbase.management.web.rest;

import static io.github.jhipster.web.util.ResponseUtil.wrapOrNotFound;
import static org.radarbase.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarbase.auth.authorization.Permission.SUBJECT_CREATE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_DELETE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_READ;
import static org.radarbase.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarbase.management.security.SecurityUtils.getJWT;
import static org.radarbase.management.web.rest.errors.EntityName.SOURCE;
import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_TYPE;
import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.EntityName.PROJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_ACTIVE_PARTICIPANT_PROJECT_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SOURCE_TYPE_NOT_PROVIDED;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Source;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.security.SecurityUtils;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.RevisionService;
import org.radarbase.management.service.SourceService;
import org.radarbase.management.service.SourceTypeService;
import org.radarbase.management.service.SubjectService;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.RevisionDTO;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.mapper.SubjectMapper;
import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.InvalidRequestException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * REST controller for managing Subject.
 */
@RestController
@RequestMapping("/api")
public class SubjectResource {

    private static final Logger log = LoggerFactory.getLogger(SubjectResource.class);

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

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private SourceService sourceService;

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
                    .createFailureAlert(SUBJECT, "projectrequired",
                            "A subject should be assigned to a project")).build();
        }
        checkPermissionOnProject(getJWT(servletRequest), SUBJECT_CREATE,
                subjectDto.getProject().getProjectName());

        if (subjectDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(SUBJECT, "idexists",
                            "A new subject cannot already have an ID")).build();
        }
        if (subjectDto.getLogin() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                            .createFailureAlert(SUBJECT, "loginrequired",
                                    "A subject login is required"))
                    .build();
        }
        if (subjectDto.getExternalId() != null && !subjectDto.getExternalId().isEmpty()
                && subjectRepository.findOneByProjectNameAndExternalId(subjectDto.getProject()
                .getProjectName(), subjectDto.getExternalId()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                            .createFailureAlert(SUBJECT, "subjectExists",
                                    "A subject with given project-id and"
                                            + "external-id already exists"))
                    .build();
        }

        SubjectDTO result = subjectService.createSubject(subjectDto);
        return ResponseEntity.created(ResourceUriService.getUri(subjectDto))
                .headers(HeaderUtil.createEntityCreationAlert(SUBJECT, result.getLogin()))
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
                    .createFailureAlert(SUBJECT, "projectrequired",
                            "A subject should be assigned to a project")).body(null);
        }
        if (subjectDto.getId() == null) {
            return createSubject(subjectDto);
        }
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDto.getProject().getProjectName(), subjectDto.getLogin());
        SubjectDTO result = subjectService.updateSubject(subjectDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(SUBJECT, subjectDto.getLogin()))
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
                            .createFailureAlert(SUBJECT, "subjectNotAvailable", "No subject found"))
                    .body(null);
        }

        if (subjectDto.getProject() == null || subjectDto.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(SUBJECT, "projectrequired",
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
                .headers(HeaderUtil.createEntityUpdateAlert(SUBJECT, subjectDto.getLogin()))
                .body(result);
    }


    /**
     * GET  /subjects : get all the subjects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of subjects in body
     */
    @GetMapping("/subjects")
    @Timed
    public ResponseEntity<List<SubjectDTO>> getAllSubjects(
            @Valid SubjectCriteria subjectCriteria
    ) throws NotAuthorizedException {
        RadarToken jwt = getJWT(servletRequest);
        String projectName = subjectCriteria.getProjectName();
        checkPermissionOnProject(jwt, SUBJECT_READ, projectName);
        if (!jwt.isClientCredentials() && jwt.hasAuthority(PARTICIPANT)) {
            throw new NotAuthorizedException("Cannot list subjects as a participant.");
        }

        String externalId = subjectCriteria.getExternalId();
        log.debug("ProjectName {} and external {}", projectName, externalId);
        // if not specified do not include inactive patients
        List<String> authoritiesToInclude = subjectCriteria.isIncludeInactive()
                ? Arrays.asList(PARTICIPANT, INACTIVE_PARTICIPANT)
                : Collections.singletonList(PARTICIPANT);

        if (projectName != null && externalId != null) {
            Optional<List<SubjectDTO>> subject = subjectRepository
                    .findOneByProjectNameAndExternalIdAndAuthoritiesIn(
                            projectName, externalId, authoritiesToInclude)
                    .map(s -> Collections.singletonList(
                            subjectMapper.subjectToSubjectReducedProjectDTO(s)));
            return wrapOrNotFound(subject);
        } else if (projectName == null && externalId != null) {
            Page<Subject> page = subjectService.findAll(subjectCriteria);
            List<SubjectDTO> items = subjectMapper
                    .subjectsToSubjectReducedProjectDTOs(page.getContent());

            HttpHeaders headers = PaginationUtil.generateSubjectPaginationHttpHeaders(
                    page, "/api/subjects", subjectCriteria);
            return new ResponseEntity<>(items, headers, HttpStatus.OK);
        } else {
            Page<SubjectDTO> page = subjectService.findAll(subjectCriteria)
                    .map(subjectMapper::subjectToSubjectWithoutProjectDTO);

            HttpHeaders headers = PaginationUtil.generateSubjectPaginationHttpHeaders(
                    page, "/api/subjects", subjectCriteria);
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
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
        Subject subject = subjectService.findOneByLogin(login);
        Project project = subject.getActiveProject()
                .flatMap(p ->  projectRepository.findOneWithEagerRelationships(p.getId()))
                .orElse(null);

        String projectName = project != null ? project.getProjectName() : null;
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, projectName,
                subject.getUser().getLogin());

        SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject);

        return ResponseEntity.ok(subjectDto);
    }

    /**
     * GET  /subjects/:login/revisions : get all revisions for the "login" subject.
     *
     * @param login the login of the subjectDTO for which to retrieve the revisions
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     *     404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/revisions")
    @Timed
    public ResponseEntity<List<RevisionDTO>> getSubjectRevisions(
            @ApiParam Pageable pageable, @PathVariable String login) throws NotAuthorizedException {
        log.debug("REST request to get revisions for Subject : {}", login);
        Subject subject = subjectService.findOneByLogin(login);
        String project = subject.getActiveProject()
                .flatMap(p -> projectRepository.findById(p.getId()))
                .map(Project::getProjectName)
                .orElseThrow(() -> new NotFoundException(
                        "Requested subject does not have an active project",
                        PROJECT,
                        ERR_ACTIVE_PARTICIPANT_PROJECT_NOT_FOUND));

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, project,
                login);
        Page<RevisionDTO> page = revisionService.getRevisionsForEntity(pageable, subject);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(page,
                        HeaderUtil.buildPath("subjects", login, "revisions")))
                .body(page.getContent());
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
    public ResponseEntity<SubjectDTO> getSubjectRevision(@PathVariable String login,
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
        Subject subject = subjectService.findOneByLogin(login);

        String projectName = subject.getActiveProject().map(Project::getProjectName).orElse(null);
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_DELETE, projectName, login);
        subjectService.deleteSubject(login);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(SUBJECT, login)).build();
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

        // find out source type id of supplied source
        Long sourceTypeId = sourceDto.getSourceTypeId();
        if (sourceTypeId == null) {
            // check if combination (producer, model, version) is present
            if (sourceDto.getSourceTypeProducer() == null
                    || sourceDto.getSourceTypeModel() == null
                    || sourceDto.getSourceTypeCatalogVersion() == null) {
                throw new BadRequestException("Producer or model or version value for the "
                        + "source-type is null" , SOURCE_TYPE, ERR_VALIDATION);
            }
            sourceTypeId = sourceTypeService
                    .findByProducerAndModelAndVersion(
                            sourceDto.getSourceTypeProducer(),
                            sourceDto.getSourceTypeModel(),
                            sourceDto.getSourceTypeCatalogVersion()).getId();
            // also update the sourceDto, since we pass it on to SubjectService later
            sourceDto.setSourceTypeId(sourceTypeId);
        }

        // check the subject id
        Subject sub = subjectService.findOneByLogin(login);

        // find the actively assigned project for this subject

        Project currentProject = sub.getActiveProject()
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "Requested subject does not have an active project",
                                SUBJECT, ERR_ACTIVE_PARTICIPANT_PROJECT_NOT_FOUND));

        // find whether the relevant source-type is available in the subject's project
        SourceType sourceType = projectRepository
                .findSourceTypeByProjectIdAndSourceTypeId(currentProject.getId(), sourceTypeId)
                .orElseThrow(() -> new BadRequestException("No valid source-type found for project."
                        + " You must provide either valid source-type id or producer, model,"
                        + " version of a source-type that is assigned to project",
                        SUBJECT, ERR_SOURCE_TYPE_NOT_PROVIDED)
                );

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE, currentProject
                .getProjectName(), sub.getUser().getLogin());

        // check if any of id, sourceID, sourceName were non-null
        boolean existing = Stream.of(sourceDto.getId(), sourceDto.getSourceName(),
                        sourceDto.getSourceId())
                .map(Objects::nonNull).reduce(false, (r1, r2) -> r1 || r2);

        // handle the source registration
        MinimalSourceDetailsDTO sourceRegistered = subjectService
                .assignOrUpdateSource(sub, sourceType, currentProject, sourceDto);

        // Return the correct response type, either created if a new source was created, or ok if
        // an existing source was provided. If an existing source was given but not found, the
        // assignOrUpdateSource would throw an error and we would not reach this point.
        if (!existing) {
            return ResponseEntity.created(ResourceUriService.getUri(sourceRegistered))
                    .headers(HeaderUtil.createEntityCreationAlert(SOURCE,
                            sourceRegistered.getSourceName()))
                    .body(sourceRegistered);
        } else {
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(SOURCE,
                    sourceRegistered.getSourceName())).body(sourceRegistered);
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

        boolean withInactiveSources = withInactiveSourcesParam != null && withInactiveSourcesParam;
        // check the subject id
        Subject subject = subjectRepository.findOneWithEagerBySubjectLogin(login)
                .orElseThrow(NoSuchElementException::new);

        String projectName = subject.getActiveProject()
                .map(Project::getProjectName)
                .orElse(null);

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, projectName, login);

        if (withInactiveSources) {
            return ResponseEntity.ok(subjectService.findSubjectSourcesFromRevisions(subject));
        } else {
            log.debug("REST request to get sources of Subject : {}", login);

            return ResponseEntity.ok(subjectService.getSources(subject));
        }
    }


    /**
     * POST  /subjects/:login/sources/:sourceName Update source attributes and source-name.
     *
     * <p>The request body is a {@link Map} of strings. This request allows
     * update of attributes only. Attributes will be merged and if a new value is
     * provided for an existing key, the new value will be updated. The request will be validated
     * for SUBJECT.UPDATE permission. SUBJECT.UPDATE is expected to keep the permissions aligned
     * with permissions from dynamic source registration and update instead of checking for
     * SOURCE_UPDATE.
     * </p>
     *
     * @param attributes The {@link Map} specification
     * @return The {@link MinimalSourceDetailsDTO} completed with all identifying fields.
     * @throws NotFoundException if the subject or the source not found using given ids.
     */
    @PostMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources/{sourceName:"
            + Constants.ENTITY_ID_REGEX + "}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "An existing source was updated"),
            @ApiResponse(code = 400, message = "You must supply existing sourceId)"),
            @ApiResponse(code = 404, message = "Either the subject or the source was not found.")
    })
    @Timed
    public ResponseEntity<MinimalSourceDetailsDTO> updateSubjectSource(@PathVariable String login,
            @PathVariable String sourceName, @RequestBody Map<String, String> attributes)
            throws NotFoundException, NotAuthorizedException,
            URISyntaxException {
        // check the subject id
        Optional<Subject> subjectOptional = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subjectOptional.isPresent()) {
            throw new NotFoundException("Subject ID not found", SUBJECT, ErrorConstants
                    .ERR_SUBJECT_NOT_FOUND, Collections.singletonMap("subjectLogin", login));
        }
        // check the permission to update source
        Subject subject = subjectOptional.get();

        String projectName = subject.getActiveProject()
                .map(Project::getProjectName)
                .orElse(null);
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE, projectName, login);

        // find source under subject
        Source source = subject.getSources().stream()
                .filter(s -> s.getSourceName().equals(sourceName))
                .findAny()
                .orElseThrow(() -> {
                    Map<String, String> errorParams = new HashMap<>();
                    errorParams.put("subjectLogin", login);
                    errorParams.put("sourceName", sourceName);
                    return new NotFoundException("Source not found under assigned sources of "
                            + "subject", SUBJECT, ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                            errorParams);
                });

        // there should be only one source under a source-name.
        return ResponseEntity.ok(sourceService.safeUpdateOfAttributes(source, attributes));
    }
}
