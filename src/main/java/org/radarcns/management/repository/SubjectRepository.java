package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Subject;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Subject entity.
 */
@SuppressWarnings("unused")
public interface SubjectRepository extends JpaRepository<Subject,Long> {

    @Query("select distinct subject from Subject subject left join fetch subject.sources")
    List<Subject> findAllWithEagerRelationships();

    @Query("select distinct subject from Subject subject left join fetch subject.sources where subject.user.project.id = :projectId")
    List<Subject> findAllByProjectId(@Param("projectId") Long projectId);

    @Query("select subject from Subject subject left join fetch subject.sources where subject.id =:id")
    Subject findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select subject from Subject subject WHERE subject.user.login = :login")
    Subject findBySubjectLogin(@Param("login") String login);

    @Query("select subject.sources from Subject subject WHERE subject.id = :id")
    List<Source> findSourcesBySubjectId(@Param("id") Long id);

    @Query("select subject.sources from Subject subject WHERE subject.user.login = :login")
    List<Source> findSourcesBySubjectLogin(@Param("login") String login);

    @Query("select distinct subject from Subject subject left join fetch subject.sources"
        +  " where subject.user.project.id = :projectId and subject.externalId = :externalId")
    Optional<Subject> findOneByProjectIdAndExternalId(@Param("projectId") Long projectId,
        @Param("externalId") String externalId);

    @Query("select subject.sources from Subject subject WHERE subject.externalId = :externalId")
    List<Subject> findAllByExternalId(@Param("externalId") String externalId);

}
