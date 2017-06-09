package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.domain.Patient;

import org.radarcns.management.domain.User;
import org.radarcns.management.repository.PatientRepository;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.PatientService;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.service.dto.PatientDTO;
import org.radarcns.management.service.mapper.PatientMapper;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/api")
public class PatientResource {

    private final Logger log = LoggerFactory.getLogger(PatientResource.class);

    private static final String ENTITY_NAME = "patient";

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientMapper patientMapper;

    /**
     * POST  /patients : Create a new patient.
     *
     * @param patientDTO the patientDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new patientDTO, or with status 400 (Bad Request) if the patient has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/patients")
    @Timed
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO) throws URISyntaxException {
        log.debug("REST request to save Patient : {}", patientDTO);
        if (patientDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new patient cannot already have an ID")).body(null);
        }
        if (patientDTO.getLogin() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "loginrequired", "A patient login is required")).body(null);
        }
        if (patientDTO.getEmail() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "emailrequired", "A patient email is required")).body(null);
        }
        PatientDTO result = patientService.createPatient(patientDTO);
        return ResponseEntity.created(new URI("/api/patients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /patients : Updates an existing patient.
     *
     * @param patientDTO the patientDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated patientDTO,
     * or with status 400 (Bad Request) if the patientDTO is not valid,
     * or with status 500 (Internal Server Error) if the patientDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/patients")
    @Timed
    public ResponseEntity<PatientDTO> updatePatient(@RequestBody PatientDTO patientDTO) throws URISyntaxException {
        log.debug("REST request to update Patient : {}", patientDTO);
        if (patientDTO.getId() == null) {
            return createPatient(patientDTO);
        }
        Patient patient = patientMapper.patientDTOToPatient(patientDTO);
        patient = patientRepository.save(patient);
        PatientDTO result = patientMapper.patientToPatientDTO(patient);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, patientDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /patients : get all the patients.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of patients in body
     */
    @GetMapping("/patients")
    @Timed
    public List<PatientDTO> getAllPatients() {
        log.debug("REST request to get all Patients");
        List<Patient> patients = patientRepository.findAllWithEagerRelationships();
        return patientMapper.patientsToPatientDTOs(patients);
    }

    /**
     * GET  /patients/:id : get the "id" patient.
     *
     * @param id the id of the patientDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the patientDTO, or with status 404 (Not Found)
     */
    @GetMapping("/patients/{id}")
    @Timed
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient : {}", id);
        Patient patient = patientRepository.findOneWithEagerRelationships(id);
        PatientDTO patientDTO = patientMapper.patientToPatientDTO(patient);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(patientDTO));
    }

    /**
     * DELETE  /patients/:id : delete the "id" patient.
     *
     * @param id the id of the patientDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/patients/{id}")
    @Timed
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.debug("REST request to delete Patient : {}", id);
        patientRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
