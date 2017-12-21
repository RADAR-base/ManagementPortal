package org.radarcns.management.repository;

import java.util.Optional;
import java.util.UUID;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Subject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Subject entity.
 */
@SuppressWarnings("unused")
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query( value = "select distinct subject from Subject subject left join fetch subject.sources",
            countQuery = "select distinct count(subject) from Subject subject")
    Page<Subject> findAllWithEagerRelationships(Pageable pageable);

    @Query(value = "select distinct subject from Subject subject left join fetch subject.sources "
            + "left join fetch subject.user user "
            + "join user.roles roles where roles.project.projectName = :projectName",
            countQuery = "select distinct count(subject) from Subject subject "
            + "left join subject.user user left join user.roles roles "
            + "where roles.project.projectName = :projectName")
    Page<Subject> findAllByProjectName(Pageable pageable, @Param("projectName") String projectName);

    @Query("select subject from Subject subject left join fetch subject.sources "
        + "where subject.id =:id")
    Subject findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select subject from Subject subject WHERE subject.user.login = :login")
    Subject findBySubjectLogin(@Param("login") String login);

    @Query("select subject from Subject subject left join fetch subject.sources "
        + "WHERE subject.user.login = :login")
    Optional<Subject> findOneWithEagerBySubjectLogin(@Param("login") String login);

    @Query("select subject.sources from Subject subject WHERE subject.id = :id")
    List<Source> findSourcesBySubjectId(@Param("id") Long id);

    @Query("select subject.sources from Subject subject WHERE subject.user.login = :login")
    List<Source> findSourcesBySubjectLogin(@Param("login") String login);

    @Query("select distinct subject from Subject subject left join fetch subject.sources "
        + "left join fetch subject.user user "
        + "join user.roles roles where roles.project.projectName = :projectName "
        + "and subject.externalId = :externalId")
    Optional<Subject> findOneByProjectNameAndExternalId(@Param("projectName") String projectName,
        @Param("externalId") String externalId);

    @Query("select subject.sources from Subject subject WHERE subject.externalId = :externalId")
    List<Subject> findAllByExternalId(@Param("externalId") String externalId);

    @Query("select subject.sources from Subject subject left join subject.sources sources "
        + "join sources.sourceType sourceType "
        + "where sourceType.producer = :producer "
        + "and sourceType.model = :model "
        + "and sourceType.catalogVersion =:version "
        + "and subject.user.login = :login")
    List<Source> findSubjectSourcesBySourceType(@Param("login") String login,
        @Param("producer") String producer, @Param("model") String model,
        @Param("version") String version);

    @Query("select distinct subject.sources from Subject subject left join subject.sources sources "
        + "where sources.sourceId= :sourceId "
        + "and subject.user.login = :login")
    Optional<Source> findSubjectSourcesBySourceId(@Param("login") String login,
        @Param("sourceId") UUID sourceId);

}
