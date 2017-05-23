package org.radarcns.management.repository;

import java.util.List;
import org.radarcns.management.domain.Patient;
import org.radarcns.management.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by nivethika on 23-5-17.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("select distinct patient from Patient patient left join fetch patient.devices")
    List<Patient> findAllWithEagerRelationships();

    @Query("select patient from Patient patient left join fetch patient.devices where patient.id =:id")
    Project findOneWithEagerRelationships(@Param("id") Long id);

}
