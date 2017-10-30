package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private HttpServletRequest servletRequest;

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
        return ResponseEntity.created(new URI("/api/subjects/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
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
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDTO.getId().toString()))
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

        SubjectDTO result = subjectService.discontinueSubject(subjectDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDTO.getId().toString()))
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
        checkPermission(SecurityUtils.getJWT(servletRequest), Permission.SUBJECT_READ);
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
    @GetMapping("/subjects/{login}")
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
    @DeleteMapping("/subjects/{login}")
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
     * POST  /subjects/:login/sources: Assign a list of sources to the currently logged in user
     *
     * The request body should contain a source-meta-data to be assigned to the a subject.
     * At minimum, each source
     * should define it's device type, like so: <code>[{"deviceType": { "id": 3 }}]</code>. A
     * source name and source ID will be automatically generated. The source ID will be a new random
     * UUID, and the source name will be the device model, appended with a dash and the first six
     * characters of the UUID. The sources will be created and assigned to the currently logged in
     * user.
     *
     * If you need to assign existing sources, simply specify either of id, sourceId, or sourceName
     * in the source object.
     *
     * @param sourceDTO List of sources to assign
     * @return The updated Subject information
     */
    @PostMapping("/subjects/{login}/sources")
    @Timed
    public ResponseEntity<MinimalSourceDetailsDTO> assignSources(@PathVariable String login,
        @RequestBody MinimalSourceDetailsDTO sourceDTO) {
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
        // find whether the relevant device-type is available in the subject's project
        Optional<DeviceType> deviceType;
        deviceType = projectRepository.findDeviceTypeByProjectIdAndDeviceTypeId(
                role.getProject().getId(), sourceDTO.getDeviceTypeId());

        if (!deviceType.isPresent()) {
            // return bad request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(HeaderUtil
                .createAlert("deviceTypeNotAvailable",
                    "No device-type found for device type ID " + sourceDTO.getDeviceTypeId()
                        + " in relevant project")).body(null);

        }

        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE, role.getProject()
                .getProjectName(), sub.getUser().getLogin());
        // handle the source registration
        MinimalSourceDetailsDTO sourceRegistered = subjectService
            .assignOrUpdateSource(sub, deviceType.get(), role.getProject(), sourceDTO);

        // TODO: replace ok() with created, with a location to query the new source.
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(
            ENTITY_NAME, sub.getId().toString())).body(sourceRegistered);
    }

    @GetMapping("/subjects/{login}/sources")
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
