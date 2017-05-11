package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.domain.Study;

import org.radarcns.management.repository.StudyRepository;
import org.radarcns.management.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Study.
 */
@RestController
@RequestMapping("/api")
public class StudyResource {

    private final Logger log = LoggerFactory.getLogger(StudyResource.class);

    private static final String ENTITY_NAME = "study";
        
    private final StudyRepository studyRepository;

    public StudyResource(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * POST  /studies : Create a new study.
     *
     * @param study the study to create
     * @return the ResponseEntity with status 201 (Created) and with body the new study, or with status 400 (Bad Request) if the study has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/studies")
    @Timed
    public ResponseEntity<Study> createStudy(@Valid @RequestBody Study study) throws URISyntaxException {
        log.debug("REST request to save Study : {}", study);
        if (study.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new study cannot already have an ID")).body(null);
        }
        Study result = studyRepository.save(study);
        return ResponseEntity.created(new URI("/api/studies/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /studies : Updates an existing study.
     *
     * @param study the study to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated study,
     * or with status 400 (Bad Request) if the study is not valid,
     * or with status 500 (Internal Server Error) if the study couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/studies")
    @Timed
    public ResponseEntity<Study> updateStudy(@Valid @RequestBody Study study) throws URISyntaxException {
        log.debug("REST request to update Study : {}", study);
        if (study.getId() == null) {
            return createStudy(study);
        }
        Study result = studyRepository.save(study);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, study.getId().toString()))
            .body(result);
    }

    /**
     * GET  /studies : get all the studies.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of studies in body
     */
    @GetMapping("/studies")
    @Timed
    public List<Study> getAllStudies() {
        log.debug("REST request to get all Studies");
        List<Study> studies = studyRepository.findAllWithEagerRelationships();
        return studies;
    }

    /**
     * GET  /studies/:id : get the "id" study.
     *
     * @param id the id of the study to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the study, or with status 404 (Not Found)
     */
    @GetMapping("/studies/{id}")
    @Timed
    public ResponseEntity<Study> getStudy(@PathVariable Long id) {
        log.debug("REST request to get Study : {}", id);
        Study study = studyRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(study));
    }

    /**
     * DELETE  /studies/:id : delete the "id" study.
     *
     * @param id the id of the study to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/studies/{id}")
    @Timed
    public ResponseEntity<Void> deleteStudy(@PathVariable Long id) {
        log.debug("REST request to delete Study : {}", id);
        studyRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
