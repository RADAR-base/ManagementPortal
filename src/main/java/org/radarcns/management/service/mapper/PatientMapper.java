package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.radarcns.management.domain.Patient;
import org.radarcns.management.service.dto.PatientDTO;

/**
 * Mapper for the entity Patient and its DTO PatientDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class, SourceMapper.class})
public interface PatientMapper {

    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "user.activated", target = "activated")
    @Mapping(source = "user.createdBy", target = "createdBy")
    @Mapping(source = "user.project", target = "project")
    @Mapping(source = "user.createdDate", target = "createdDate")
    @Mapping(source = "user.lastModifiedBy", target = "lastModifiedBy")
    @Mapping(source = "user.lastModifiedDate", target = "lastModifiedDate")
    @Mapping(target = "email", ignore = true)
    PatientDTO patientToPatientDTO(Patient patient);

    List<PatientDTO> patientsToPatientDTOs(List<Patient> patients);

    @Mapping(source = "login", target = "user.login")
    @Mapping(source = "activated", target = "user.activated")
    @Mapping(source = "createdBy", target = "user.createdBy")
    @Mapping(source = "project", target = "user.project")
    @Mapping(source = "createdDate", target = "user.createdDate")
    @Mapping(source = "lastModifiedBy", target = "user.lastModifiedBy")
    @Mapping(source = "lastModifiedDate", target = "user.lastModifiedDate")
    @Mapping(target = "user.email" , ignore = true)
    Patient patientDTOToPatient(PatientDTO patientDTO);

    @Mapping(target = "user", ignore = true)
    Patient safeUpdatePatientFromDTO(PatientDTO patientDTO, @MappingTarget Patient patient);

    List<Patient> patientDTOsToPatients(List<PatientDTO> patientDTOs);
    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it, instead of
     * creating a new attribute to know if the entity has any relationship from some other entity
     *
     * @param id id of the entity
     * @return the entity instance
     */

    default Patient patientFromId(Long id) {
        if (id == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setId(id);
        return patient;
    }


}
