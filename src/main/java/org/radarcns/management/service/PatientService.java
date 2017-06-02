package org.radarcns.management.service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.Patient;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.PatientRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.PatientDTO;
import org.radarcns.management.service.mapper.PatientMapper;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private ProjectMapper projectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public PatientDTO createPatient(PatientDTO patientDTO) {
        Patient patient = patientMapper.patientDTOToPatient(patientDTO);
        patient.setUser(setOtherPropertiesToPatientUser(patient.getUser()));
        patient = patientRepository.save(patient);
        if (patient.getId() != null) {
            patient.getUser().setEmail(patientDTO.getEmail());
            mailService.sendCreationEmail(patient.getUser());
        }
        return patientMapper.patientToPatientDTO(patient);
    }

    private User setOtherPropertiesToPatientUser(User user) {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findRoleByAuthorityName(AuthoritiesConstants.PARTICIPANT);
        if (role != null) {
            roles.add(role);
        }
        user.setRoles(roles);
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setLangKey(
            "en"); // setting default language key to "en", required to set email context,
        // Find a workaround
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(false);
        return user;
    }

    public PatientDTO updatePatient(PatientDTO patientDTO) {
        if (patientDTO.getId() == null) {
            return createPatient(patientDTO);
        }
        //  TODO : add security and owner check for the resource
        Patient patient = patientRepository.findOne(patientDTO.getId());

        patientMapper.safeUpdatePatientFromDTO(patientDTO, patient);
        patient.getUser().setProject(projectMapper.projectDTOToProject(patientDTO.getProject()));
        patient = patientRepository.save(patient);

        return patientMapper.patientToPatientDTO(patient);
    }


}
