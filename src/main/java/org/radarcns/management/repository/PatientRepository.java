package org.radarcns.management.repository;

import org.radarcns.management.domain.Patient;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Patient entity.
 */
@SuppressWarnings("unused")
public interface PatientRepository extends JpaRepository<Patient,Long> {

    @Query("select distinct patient from Patient patient left join fetch patient.devices")
    List<Patient> findAllWithEagerRelationships();

    @Query("select patient from Patient patient left join fetch patient.devices where patient.id =:id")
    Patient findOneWithEagerRelationships(@Param("id") Long id);

}
