package org.radarbase.management.repository;

import org.radarbase.management.domain.Source;
import org.radarbase.management.domain.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Subject entity.
 */
@SuppressWarnings("unused")
@RepositoryDefinition(domainClass = Subject.class, idClass = Long.class)
public interface SubjectRepository extends JpaRepository<Subject, Long>,
        RevisionRepository<Subject, Long, Integer>,
        JpaSpecificationExecutor<Subject> {

    @Query("SELECT count(*) from Subject subject "
            + "WHERE subject.group.id = :group_id")
    long countByGroupId(@Param("group_id") Long groupId);

    @Query(value = "select distinct subject from Subject subject left join fetch subject.sources "
            + "left join fetch subject.user user "
            + "join user.roles roles where roles.project.projectName = :projectName and roles"
            + ".authority.name in :authorities",
            countQuery = "select distinct count(subject) from Subject subject "
                    + "left join subject.user user left join user.roles roles "
                    + "where roles.project.projectName = :projectName and roles"
                    + ".authority.name in :authorities")
    Page<Subject> findAllByProjectNameAndAuthoritiesIn(Pageable pageable,
            @Param("projectName") String projectName,
            @Param("authorities") List<String> authorities);

    @Query("select subject from Subject subject left join fetch subject.sources "
            + "WHERE subject.user.login = :login")
    Optional<Subject> findOneWithEagerBySubjectLogin(@Param("login") String login);

    @Query("select subject from Subject subject "
            + "WHERE subject.user.login in :logins")
    List<Subject> findAllBySubjectLogins(@Param("logins") List<String> logins);

    @Modifying
    @Query("UPDATE Subject subject "
            + "SET subject.group.id = :groupId "
            + "WHERE subject.id in :ids")
    void setGroupIdByIds(
            @Param("groupId") Long groupId,
            @Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE Subject subject "
            + "SET subject.group.id = null "
            + "WHERE subject.id in :ids")
    void unsetGroupIdByIds(@Param("ids") List<Long> ids);

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

    @Query("select distinct subject from Subject subject left join fetch subject.sources "
            + "left join fetch subject.user user "
            + "join user.roles roles where roles.project.projectName = :projectName "
            + "and roles.authority.name in :authorities "
            + "and subject.externalId = :externalId")
    Optional<Subject> findOneByProjectNameAndExternalIdAndAuthoritiesIn(@Param("projectName") String
            projectName, @Param("externalId") String externalId,
            @Param("authorities") List<String> authorities);

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


    @Query("select subject.externalId from Subject subject")
    List<String> findAllExternalIds();


}
