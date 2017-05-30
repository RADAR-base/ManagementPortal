package org.radarcns.management.service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.Patient;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.PatientRepository;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.dto.PatientDTO;
import org.radarcns.management.service.mapper.PatientMapper;
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
    private PatientRepository patientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public PatientDTO createPatient(PatientDTO patientDTO) {
        Patient patient = patientMapper.patientDTOToPatient(patientDTO);
        User user = createPatientUser(patient.getUser());
        patient.setUser(user);
        patient = patientRepository.save(patient);
        if(patient.getId() !=null) {
            User patientUser = patient.getUser();
            patientUser.setEmail(patientDTO.getEmail());
            mailService.sendCreationEmail(patientUser);
        }
        PatientDTO result = patientMapper.patientToPatientDTO(patient);
        return result;
    }

    public User createPatientUser(User user) {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByAuthorityName(AuthoritiesConstants.PARTICIPANT);
        if (role != null) {
            roles.add(role);
        }
        user.setRoles(roles);
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setLangKey("en");
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(false);
        return user;
    }


}
