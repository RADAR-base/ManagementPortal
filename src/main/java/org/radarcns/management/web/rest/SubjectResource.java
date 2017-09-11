package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        if (subjectDTO.getProject().getId() == null) {
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

        SubjectDTO result = subjectService.updateSubject(subjectDTO);
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
        @RequestParam(value = "projectId" , required = false) Long projectId,
        @RequestParam(value = "externalId" , required = false) String externalId) {
        log.error("ProjectID {} and external {}" , projectId, externalId);
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
}
