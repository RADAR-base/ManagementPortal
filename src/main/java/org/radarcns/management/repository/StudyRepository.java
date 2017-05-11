package org.radarcns.management.repository;

import org.radarcns.management.domain.Study;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Study entity.
 */
@SuppressWarnings("unused")
public interface StudyRepository extends JpaRepository<Study,Long> {

    @Query("select distinct study from Study study left join fetch study.devices")
    List<Study> findAllWithEagerRelationships();

    @Query("select study from Study study left join fetch study.devices where study.id =:id")
    Study findOneWithEagerRelationships(@Param("id") Long id);

}
