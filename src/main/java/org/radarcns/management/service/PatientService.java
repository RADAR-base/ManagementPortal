package org.radarcns.management.service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Device;
import org.radarcns.management.domain.Patient;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.DeviceRepository;
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
    private AuthorityRepository authorityRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;


    @Transactional
    public PatientDTO createPatient(PatientDTO patientDTO) throws IllegalAccessException {
        User currentUser = userService.getUserWithAuthorities();
        Patient patient = patientMapper.patientDTOToPatient(patientDTO);
        List<String> currentUserAuthorities = currentUser.getAuthorities().stream()
            .map(Authority::getName).collect(
                Collectors.toList());
        if (currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)
            && !currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN) && !currentUser.getProject().equals(patient.getUser().getProject())) {
            log.debug("Validate project admin");
            throw new IllegalAccessException("This project-admin is not allowed to create Patients under this project");
        }


        User user = patient.getUser();
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository
            .findOneByAuthorityNameAndProjectId(AuthoritiesConstants.PARTICIPANT,
                patient.getUser().getProject().getId());
        if (role != null) {
            roles.add(role);
        } else {
            Role patientRole = new Role();
            patientRole.setAuthority(
                authorityRepository.findByAuthorityName(AuthoritiesConstants.PARTICIPANT));
            patientRole.setProject(patient.getUser().getProject());
            roleRepository.save(patientRole);
            roles.add(patientRole);
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
        patient.setUser(user);
        patient = patientRepository.save(patient);
        if (patient.getId() != null) {
            mailService.sendCreationEmailForGivenEmail(patient.getUser(), patientDTO.getEmail());
        }
        //set if any devices are set as assigned
        if(patient.getDevices() !=null && !patient.getDevices().isEmpty()) {
            for (Device device: patient.getDevices()) {
                device.setAssigned(true);
            }
        }
        return patientMapper.patientToPatientDTO(patient);
    }

    @Transactional
    public PatientDTO updatePatient(PatientDTO patientDTO) throws IllegalAccessException {
        if (patientDTO.getId() == null) {
            return createPatient(patientDTO);
        }
        //  TODO : add security and owner check for the resource
        Patient patient = patientRepository.findOne(patientDTO.getId());
        //reset all the devices assigned to a patient to unassigned
        for(Device device: patient.getDevices()) {
            device.setAssigned(false);
            deviceRepository.save(device);
        }
        //set only the devices assigned to a patient as assigned
        patientMapper.safeUpdatePatientFromDTO(patientDTO, patient);
        for(Device device: patient.getDevices()) {
            device.setAssigned(true);
        }
        patient.getUser().setProject(projectMapper.projectDTOToProject(patientDTO.getProject()));
        patient = patientRepository.save(patient);

        return patientMapper.patientToPatientDTO(patient);
    }


}
