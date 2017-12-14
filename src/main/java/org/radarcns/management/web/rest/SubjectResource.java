package org.radarcns.management.web.rest;

import static org.radarcns.auth.authorization.Permission.SUBJECT_CREATE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_DELETE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarcns.management.security.SecurityUtils.getJWT;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
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
     * @param subjectDTO the subjectDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subjectDTO, or
     * with status 400 (Bad Request) if the subject has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/subjects")
    @Timed
    public ResponseEntity<SubjectDTO> createSubject(@RequestBody SubjectDTO subjectDTO)
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to save Subject : {}", subjectDTO);
        if (subjectDTO.getProject() == null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "projectrequired",
                            "A subject should be assigned to a project")).body(null);
        }
        checkPermissionOnProject(getJWT(servletRequest), SUBJECT_CREATE,
                subjectDTO.getProject().getProjectName());

        if (subjectDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "idexists",
                    "A new subject cannot already have an ID")).body(null);
        }
        if (subjectDTO.getLogin() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "loginrequired", "A subject login is required"))
                .body(null);
        }
        if (subjectDTO.getExternalId() != null && !subjectDTO.getExternalId().isEmpty() &&
            subjectRepository.findOneByProjectNameAndExternalId(subjectDTO.getProject().getProjectName(),
                subjectDTO.getExternalId()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "subjectExists",
                    "A subject with given project-id and external-id already exists")).body(null);
        }

        SubjectDTO result = subjectService.createSubject(subjectDTO);
        return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "subjects",
                result.getLogin())))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getLogin()))
                .body(result);
    }

    /**
     * PUT  /subjects : Updates an existing subject.
     *
     * @param subjectDTO the subjectDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDTO, or with
     * status 400 (Bad Request) if the subjectDTO is not valid, or with status 500 (Internal Server
     * Error) if the subjectDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects")
    @Timed
    public ResponseEntity<SubjectDTO> updateSubject(@RequestBody SubjectDTO subjectDTO)
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to update Subject : {}", subjectDTO);
        if (subjectDTO.getProject() == null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "projectrequired",
                            "A subject should be assigned to a project")).body(null);
        }
        if (subjectDTO.getId() == null) {
            checkPermissionOnProject(getJWT(servletRequest), SUBJECT_CREATE,
                    subjectDTO.getProject().getProjectName());
            return createSubject(subjectDTO);
        }
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDTO.getProject().getProjectName(), subjectDTO.getLogin());
        SubjectDTO result = subjectService.updateSubject(subjectDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDTO.getLogin()))
            .body(result);
    }

    /**
     * PUT  /subjects/discontinue : Discontinue a subject. A discontinued subject is not allowed
     * to send data to the system anymore.
     *
     * @param subjectDTO the subjectDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDTO, or with
     * status 400 (Bad Request) if the subjectDTO is not valid, or with status 500 (Internal Server
     * Error) if the subjectDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects/discontinue")
    @Timed
    public ResponseEntity<SubjectDTO> discontinueSubject(@RequestBody SubjectDTO subjectDTO)
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to update Subject : {}", subjectDTO);
        if (subjectDTO.getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "subjectNotAvailable", "No subject found"))
                .body(null);
        }

        if (subjectDTO.getProject() == null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "projectrequired",
                    "A subject should be assigned to a project")).body(null);
        }
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDTO.getProject().getProjectName(), subjectDTO.getLogin());

        // In principle this is already captured by the PostUpdate event listener, adding this
        // event just makes it more clear a subject was discontinued.
        eventRepository.add(new AuditEvent(SecurityUtils.getCurrentUserLogin(),
                "SUBJECT_DISCONTINUE", "subject_login=" + subjectDTO.getLogin()));
        SubjectDTO result = subjectService.discontinueSubject(subjectDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDTO.getLogin()))
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
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "externalId", required = false) String externalId) {
        checkPermission(getJWT(servletRequest), SUBJECT_READ);
        log.debug("ProjectName {} and external {}", projectName, externalId);
        if (projectName != null && externalId != null) {
            Subject subject = subjectRepository
                .findOneByProjectNameAndExternalId(projectName, externalId).get();
            SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject);
            return ResponseUtil.wrapOrNotFound(Optional.of(Collections.singletonList(subjectDTO)));
        } else if (projectName == null && externalId != null) {
            List<Subject> subjects = subjectRepository.findAllByExternalId(externalId);
            return ResponseUtil
                .wrapOrNotFound(Optional.of(subjectMapper.subjectsToSubjectDTOs(subjects)));
        } else if (projectName != null) {
            List<Subject> subjects = subjectRepository.findAllByProjectName(projectName);
            return ResponseUtil
                .wrapOrNotFound(Optional.of(subjectMapper.subjectsToSubjectDTOs(subjects)));
        }
        log.debug("REST request to get all Subjects");
        return ResponseEntity.ok(subjectService.findAll());
    }

    /**
     * GET  /subjects/:login : get the "login" subject.
     *
     * @param login the login of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable String login) {
        log.debug("REST request to get Subject : {}", login);
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, subjectDTO.getProject()
            .getProjectName(), subjectDTO.getLogin());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(subjectDTO));
    }

    /**
     * DELETE  /subjects/:login : delete the "login" subject.
     *
     * @param login the login of the subjectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSubject(@PathVariable String login) {
        log.debug("REST request to delete Subject : {}", login);
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_DELETE, subjectDTO.getProject()
            .getProjectName(), subjectDTO.getLogin());
        subjectService.deleteSubject(login);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, login)).build();
    }

    /**
     * POST  /subjects/:login/sources: Assign a source to the specified user
     *
     * The request body is a {@link MinimalSourceDetailsDTO}. At minimum, the source should
     * define it's source type by either supplying the sourceTypeId, or the combination of
     * (sourceTypeProducer, sourceTypeModel, sourceTypeCatalogVersion) fields. A source ID will
     * be automatically generated. The source ID will be a new random UUID, and the source name,
     * if not provided, will be the device model, appended with a dash and the first eight
     * characters of the UUID. The sources will be created and assigned to the specified user.
     *
     * If you need to assign existing sources, simply specify either of id, sourceId, or sourceName
     * fields.
     *
     * @param sourceDTO The {@link MinimalSourceDetailsDTO} specification
     * @return The {@link MinimalSourceDetailsDTO} completed with all identifying fields.
     *
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
            @RequestBody MinimalSourceDetailsDTO sourceDTO) throws URISyntaxException {
        // check the subject id
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseUtil.wrapOrNotFound(Optional.empty(), HeaderUtil.createFailureAlert(
                ENTITY_NAME, "notfound", "Subject with subject-id " + login +
                    " was not found."));
        }
        Subject sub = subject.get();
        // find the PARTICIPANT role for this subject
        Optional<Role> roleOptional = sub.getUser().getRoles().stream()
                .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PARTICIPANT))
                .findFirst();
        if (!roleOptional.isPresent()) {
            // no participant role found
            return ResponseUtil.wrapOrNotFound(Optional.empty(), HeaderUtil.createFailureAlert(
                ENTITY_NAME, "notfound", "Subject with subject-id " + login +
                    " is not assigned to any project. Could not find project for this subject."));
        }
        Role role = roleOptional.get();
        // find out source type id of supplied source
        Long sourceTypeId = sourceDTO.getSourceTypeId();
        if (Objects.isNull(sourceTypeId)) {
            // check if combination (producer, model, version) is present
            final String msg = "You must supply either the sourceTypeId, or the combination of "
                    + "(sourceTypeProducer, sourceTypeModel, catalogVersion) fields.";
            try {
                String producer = Objects.requireNonNull(sourceDTO.getSourceTypeProducer(), msg);
                String model = Objects.requireNonNull(sourceDTO.getSourceTypeModel(), msg);
                String version = Objects.requireNonNull(sourceDTO.getSourceTypeCatalogVersion(),
                        msg);
                SourceTypeDTO sourceTypeDTO = sourceTypeService
                        .findByProducerAndModelAndVersion(producer, model, version);
                if (Objects.isNull(sourceTypeDTO)) {
                    return ResponseEntity.notFound().build();
                }
                sourceTypeId = sourceTypeDTO.getId();
            } catch (NullPointerException ex) {
                log.error(ex.getMessage() + ", supplied sourceDTO: " + sourceDTO.toString());
                throw new CustomParameterizedException(ex.getMessage());
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
                    "No source-type found for source type ID " + sourceDTO.getSourceTypeId()
                        + " in relevant project")).body(null);
        }

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE, role.getProject()
                .getProjectName(), sub.getUser().getLogin());

        // check if any of id, sourceID, sourceName were non-null
        boolean existing = Stream.of(sourceDTO.getId(), sourceDTO.getSourceName(),
                sourceDTO.getSourceId())
                .map(Objects::nonNull).reduce(false, (r1, r2) -> r1 || r2);

        // handle the source registration
        MinimalSourceDetailsDTO sourceRegistered = subjectService
                .assignOrUpdateSource(sub, sourceType.get(), role.getProject(), sourceDTO);

        // Return the correct response type, either created if a new source was created, or ok if
        // an existing source was provided. If an existing source was given but not found, the
        // assignOrUpdateSource would throw an error and we would not reach this point.
        if (!existing) {
            return ResponseEntity.created(new URI("/api/sources/" + sourceRegistered.getSourceName())).headers(
                    HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, login))
                    .body(sourceRegistered);
        }
        else {
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME,
                    login)).body(sourceRegistered);
        }
    }

    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @Timed
    public ResponseEntity<List<MinimalSourceDetailsDTO>> getSubjectSources(
        @PathVariable String login) {
        // check the subject id
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (!subject.isPresent()) {
            return ResponseUtil.wrapOrNotFound(Optional.empty(), HeaderUtil.createFailureAlert(
                ENTITY_NAME, "notfound", "Subject with subject-id " + login +
                    " was not found."));
        }

        SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject.get());
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_READ, subjectDTO.getProject()
                .getProjectName(), subjectDTO.getLogin());

        // handle the source registration
        List<MinimalSourceDetailsDTO> sources = subjectService.getSources(subject.get());

        return ResponseEntity.ok().body(sources);
    }
}
