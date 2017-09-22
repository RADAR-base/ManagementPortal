package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.dto.SourceRegistrationDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private SourceMapper sourceMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;
    /**
     * POST  /subjects : Create a new subject.
     *
     * @param subjectDTO the subjectDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subjectDTO, or with status 400 (Bad Request) if the subject has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/subjects")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN , AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR})
    public ResponseEntity<SubjectDTO> createSubject(@RequestBody SubjectDTO subjectDTO)
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to save Subject : {}", subjectDTO);
        if (subjectDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new subject cannot already have an ID")).body(null);
        }
        if (subjectDTO.getLogin() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "loginrequired", "A subject login is required")).body(null);
        }
        if (subjectDTO.getEmail() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "patientEmailRequired", "A subject email is required")).body(null);
        }
        if (subjectDTO.getProject()==null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "projectrequired", "A subject should be assigned to a project")).body(null);
        }
        if (subjectDTO.getExternalId() != null && !subjectDTO.getExternalId().isEmpty() &&
            subjectRepository.findOneByProjectIdAndExternalId(subjectDTO.getProject().getId() , subjectDTO.getExternalId()).isPresent()) {
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
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDTO,
     * or with status 400 (Bad Request) if the subjectDTO is not valid,
     * or with status 500 (Internal Server Error) if the subjectDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN , AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR})
    public ResponseEntity<SubjectDTO> updateSubject(@RequestBody SubjectDTO subjectDTO)
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to update Subject : {}", subjectDTO);
        if (subjectDTO.getId() == null) {
            return createSubject(subjectDTO);
        }

        if (subjectDTO.getProject()==null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "projectrequired", "A subject should be assigned to a project")).body(null);
        }
        if (subjectDTO.getExternalId() != null && !subjectDTO.getExternalId().isEmpty() &&
            subjectRepository.findOneByProjectIdAndExternalId(subjectDTO.getProject().getId() , subjectDTO.getExternalId()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                .createFailureAlert(ENTITY_NAME, "subjectExists",
                    "A subject with given project-id and external-id already exists")).body(null);
        }
        SubjectDTO result = subjectService.updateSubject(subjectDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, subjectDTO.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /subjects : Updates an existing subject.
     *
     * @param subjectDTO the subjectDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDTO,
     * or with status 400 (Bad Request) if the subjectDTO is not valid,
     * or with status 500 (Internal Server Error) if the subjectDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects/discontinue")
    @Timed
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN })
    public ResponseEntity<SubjectDTO> discontinueSubject(@RequestBody SubjectDTO subjectDTO )
        throws URISyntaxException, IllegalAccessException {
        log.debug("REST request to update Subject : {}", subjectDTO);
        if (subjectDTO.getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "subjectNotAvailable", "No subject found")).body(null);
        }

        if (subjectDTO.getProject()==null || subjectDTO.getProject().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "projectrequired", "A subject should be assigned to a project")).body(null);
        }

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
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN , AuthoritiesConstants.EXTERNAL_ERF_INTEGRATOR})
    public ResponseEntity<List<SubjectDTO>> getAllSubjects(
        @RequestParam(value = "projectId" , required = false) Long projectId,
        @RequestParam(value = "externalId" , required = false) String externalId) {
        log.debug("ProjectID {} and external {}" , projectId, externalId);
        if(projectId!=null && externalId!=null) {
            Subject subject = subjectRepository.findOneByProjectIdAndExternalId(projectId, externalId).get();
            SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject);
            return ResponseUtil.wrapOrNotFound(Optional.of(Collections.singletonList(subjectDTO)));
        }
        else if (projectId==null && externalId!=null) {
            List<Subject> subjects = subjectRepository.findAllByExternalId(externalId);
            return ResponseUtil.wrapOrNotFound(Optional.of(subjectMapper.subjectsToSubjectDTOs(subjects)));
        }
        else if( projectId!=null) {
            List<Subject> subjects = subjectRepository.findAllByProjectId(projectId);
            return ResponseUtil.wrapOrNotFound(Optional.of(subjectMapper.subjectsToSubjectDTOs(subjects)));
        }
        log.debug("REST request to get all Subjects");
        return ResponseEntity.ok(subjectService.findAll());
    }

    /**
     * GET  /subjects/:id : get the "id" subject.
     *
     * @param id the id of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status 404 (Not Found)
     */
    @GetMapping("/subjects/{id}")
    @Timed
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable Long id) {
        log.debug("REST request to get Subject : {}", id);
        Subject subject = subjectRepository.findOneWithEagerRelationships(id);
        SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(subjectDTO));
    }

    /**
     * DELETE  /subjects/:id : delete the "id" subject.
     *
     * @param id the id of the subjectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/subjects/{id}")
    @Timed
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        log.debug("REST request to delete Subject : {}", id);
        subjectRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * POST  /subjects/:login/sources: Assign a list of sources to the currently logged in user
     *
     * The request body should contain a list of sources to be assigned to the currently logged in
     * user. If the currently authenticated user is not a subject, or not a user
     * (e.g. client_credentials), an AccessDeniedException will be thrown. At minimum, each source
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
    @Secured({AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN,
        AuthoritiesConstants.PARTICIPANT})
    public ResponseEntity<SourceRegistrationDTO> assignSources(@PathVariable String login,
        @RequestBody SourceRegistrationDTO sourceDTO) {
        // check the subject id
        Subject subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        if (subject == null) {
            return ResponseUtil.wrapOrNotFound(Optional.empty(), HeaderUtil.createFailureAlert(
                ENTITY_NAME, "notfound", "Subject with subject-id " + login +
                    " was not found."));
        }

        Role role =  subject.getUser().getRoles().stream().findFirst().get();
        // find whether the relevant device-type is available in the subject's project
        Optional<DeviceType> deviceType = projectRepository
            .findDeviceTypeByProjectIdAndDeviceTypeProp(role.getProject().getId(),
                sourceDTO.getDeviceTypeProducer(),
                sourceDTO.getDeviceTypeModel(),
                sourceDTO.getDeviceTypeVersion());
        if (!deviceType.isPresent()) {
            // return bad request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(HeaderUtil
                .createAlert("deviceTypeNotAvailable",
                    "No device-type found for producer " + sourceDTO.getDeviceTypeProducer()
                        + " , model " + sourceDTO.getDeviceTypeModel() + " and version " + sourceDTO
                        .getDeviceTypeVersion()+" in relevant project")).body(null);

        }

        // handle the source registration
        Source source = subjectService.assignSource(subject, deviceType.get(), role.getProject(), sourceDTO);

        sourceDTO.setSourceId(source.getSourceId());
        sourceDTO.setExpectedSourceName(source.getDeviceType().getDeviceModel().concat("_: "+source.getSourceName()));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(
            ENTITY_NAME, subject.getId().toString())).body(sourceDTO);
    }

    /**
     * GET   /subjects/:id/sources: Get the sources of the currently logged in user.
     *
     * @return The list of sources assigned to the currently logged in user
     */
    @GetMapping("/subjects/{id}/sources")
    @Timed
    public ResponseEntity<List<SourceDTO>> getSources(@PathVariable Long id) {

        Subject subject = subjectRepository.findOneWithEagerRelationships(id);
        if (subject == null) {
            return ResponseUtil.wrapOrNotFound(Optional.empty(), HeaderUtil.createFailureAlert(
                ENTITY_NAME, "notfound", "Subject with id " + id.toString() +
                    " was not found."));
        }
        List<SourceDTO> result = sourceMapper.sourcesToSourceDTOs(new ArrayList<>(subject.getSources()));
        return ResponseEntity.ok(result);
    }
}
