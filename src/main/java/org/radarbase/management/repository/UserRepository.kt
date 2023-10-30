package org.radarbase.management.repository

import org.radarbase.management.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

/**
 * Spring Data JPA repository for the User entity.
 */
@RepositoryDefinition(domainClass = User::class, idClass = Long::class)
@Component
interface UserRepository : JpaRepository<User, Long>, RevisionRepository<User, Long, Int>,
    JpaSpecificationExecutor<User> {
    fun findOneByActivationKey(activationKey: String): User?
    fun findAllByActivated(activated: Boolean): List<User>

    @Query(
        "select user from User user "
                + "left join fetch user.roles roles where "
                + "roles.authority.name not in :authorities "
                + "and user.activated= :activated"
    )
    fun findAllByActivatedAndAuthoritiesNot(
        @Param("activated") activated: Boolean,
        @Param("authorities") authorities: List<String>
    ): List<User>

    fun findOneByResetKey(resetKey: String): User?
    fun findOneByEmail(email: String): User?
    fun findOneByLogin(login: String): User?

    @EntityGraph(attributePaths = ["roles", "roles.authority.name"])
    fun findOneWithRolesByLogin(login: String): User?
    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>
}
