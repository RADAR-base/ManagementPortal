package org.radarcns.management.service;

import org.radarcns.management.domain.Patient;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.PatientRepository;
import org.radarcns.management.service.dto.PatientDTO;
import org.radarcns.management.service.mapper.PatientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika on 26-5-17.
 */
@Service
@Transactional
public class PatientService {

    private final Logger log = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;


    public PatientDTO createPatient(PatientDTO patientDTO) {
        Patient patient = patientMapper.patientDTOToPatient(patientDTO);
        User user = userService.createPatientUser(patient.getUser());
        patient.setUser(user);
        patient = patientRepository.save(patient);
        PatientDTO result = patientMapper.patientToPatientDTO(patient);
        User patientUser = patient.getUser();
        patientUser.setEmail(patientDTO.getEmail());
        mailService.sendCreationEmail(patientUser);
        return result;
    }



}
