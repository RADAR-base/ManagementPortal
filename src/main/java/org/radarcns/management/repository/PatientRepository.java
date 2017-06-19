package org.radarcns.management.repository;

import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Patient;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Patient entity.
 */
@SuppressWarnings("unused")
public interface PatientRepository extends JpaRepository<Patient,Long> {

    @Query("select distinct patient from Patient patient left join fetch patient.sources")
    List<Patient> findAllWithEagerRelationships();

    @Query("select patient from Patient patient left join fetch patient.sources where patient.id =:id")
    Patient findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select patient.sources from Patient patient WHERE patient.id = :id")
    List<Source> findSourcesByPatientId(@Param("id") Long id);

    @Query("select patient.sources from Patient patient WHERE patient.user.login = :login")
    List<Source> findSourcesByPatientLogin(@Param("login") String login);

}
