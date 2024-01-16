package org.radarbase.management.repository

import org.radarbase.management.domain.Source
import org.radarbase.management.domain.Subject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Spring Data JPA repository for the Subject entity.
 */
@Suppress("unused")
@RepositoryDefinition(domainClass = Subject::class, idClass = Long::class)
interface SubjectRepository : JpaRepository<Subject, Long?>, RevisionRepository<Subject, Long?, Int>,
    JpaSpecificationExecutor<Subject> {
    @Query(
        "SELECT count(*) from Subject subject "
                + "WHERE subject.group.id = :group_id"
    )
    fun countByGroupId(@Param("group_id") groupId: Long?): Long

    @Query(
        value = "select distinct subject from Subject subject left join fetch subject.sources "
                + "left join fetch subject.user user "
                + "join user.roles roles where roles.project.projectName = :projectName and roles"
                + ".authority.name in :authorities", countQuery = "select distinct count(subject) from Subject subject "
                + "left join subject.user user left join user.roles roles "
                + "where roles.project.projectName = :projectName and roles"
                + ".authority.name in :authorities"
    )
    fun findAllByProjectNameAndAuthoritiesIn(
        pageable: Pageable?,
        @Param("projectName") projectName: String?,
        @Param("authorities") authorities: List<String?>?
    ): Page<Subject?>?

    @Query(
        "select subject from Subject subject "
                + "left join fetch subject.sources "
                + "WHERE subject.user.login = :login"
    )
    fun findOneWithEagerBySubjectLogin(@Param("login") login: String?): Subject?

    @Query(
        "select subject from Subject subject "
                + "WHERE subject.user.login in :logins"
    )
    fun findAllBySubjectLogins(@Param("logins") logins: List<String>): List<Subject>

    @Modifying
    @Query(
        "UPDATE Subject subject "
                + "SET subject.group.id = :groupId "
                + "WHERE subject.id in :ids"
    )
    fun setGroupIdByIds(
        @Param("groupId") groupId: Long,
        @Param("ids") ids: List<Long>
    )

    @Modifying
    @Query(
        "UPDATE Subject subject "
                + "SET subject.group.id = null "
                + "WHERE subject.id in :ids"
    )
    fun unsetGroupIdByIds(@Param("ids") ids: List<Long>)

    @Query("select subject.sources from Subject subject WHERE subject.id = :id")
    fun findSourcesBySubjectId(@Param("id") id: Long): List<Source>

    @Query("select subject.sources from Subject subject WHERE subject.user.login = :login")
    fun findSourcesBySubjectLogin(@Param("login") login: String?): List<Source>

    @Query(
        "select distinct subject from Subject subject left join fetch subject.sources "
                + "left join fetch subject.user user "
                + "join user.roles roles where roles.project.projectName = :projectName "
                + "and subject.externalId = :externalId"
    )
    fun findOneByProjectNameAndExternalId(
        @Param("projectName") projectName: String?,
        @Param("externalId") externalId: String?
    ): Subject?

    @Query(
        "select distinct subject from Subject subject left join fetch subject.sources "
                + "left join fetch subject.user user "
                + "join user.roles roles where roles.project.projectName = :projectName "
                + "and roles.authority.name in :authorities "
                + "and subject.externalId = :externalId"
    )
    fun findOneByProjectNameAndExternalIdAndAuthoritiesIn(
        @Param("projectName") projectName: String, @Param("externalId") externalId: String,
        @Param("authorities") authorities: List<String>
    ): Subject?

    @Query(
        "select subject.sources from Subject subject left join subject.sources sources "
                + "join sources.sourceType sourceType "
                + "where sourceType.producer = :producer "
                + "and sourceType.model = :model "
                + "and sourceType.catalogVersion =:version "
                + "and subject.user.login = :login"
    )
    fun findSubjectSourcesBySourceType(
        @Param("login") login: String?,
        @Param("producer") producer: String?, @Param("model") model: String?,
        @Param("version") version: String?
    ): List<Source>?

    @Query(
        "select distinct subject.sources from Subject subject left join subject.sources sources "
                + "where sources.sourceId= :sourceId "
                + "and subject.user.login = :login"
    )
    fun findSubjectSourcesBySourceId(
        @Param("login") login: String?,
        @Param("sourceId") sourceId: UUID?
    ): Source?

    @Query("select subject.externalId from Subject subject")
    fun findAllExternalIds(): List<String?>?
}
