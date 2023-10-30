package org.radarbase.management.repository.filters

import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.domain.Organization
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import java.util.function.BiConsumer
import java.util.stream.Stream
import javax.annotation.Nonnull
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.From
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class UserFilter : Specification<User?> {
    var login: String? = null
    var email: String? = null
    var projectName: String? = null
    var organization: String? = null
    var authority: String? = null
    var isIncludeUpperLevels = false
    override fun toPredicate(
        root: Root<User?>, @Nonnull query: CriteriaQuery<*>,
        @Nonnull builder: CriteriaBuilder
    ): Predicate {
        val predicates = PredicateBuilder(builder)
        predicates.likeLower(root.get("login"), login)
        predicates.likeLower(root.get("email"), email)
        filterRoles(predicates, root.join("roles", JoinType.LEFT), query)
        query.distinct(true)
        val result = predicates.toAndPredicate()
        logger.debug("Filtering users by {}", result)
        return result!!
    }

    private fun filterRoles(
        predicates: PredicateBuilder, roleJoin: Join<User, Role>,
        query: CriteriaQuery<*>
    ) {
        var authoritiesFiltered = Stream.of(*RoleAuthority.values())
            .filter { r: RoleAuthority -> !r.isPersonal }
        var allowNoRole = true
        if (predicates.isValidValue(authority)) {
            val authorityUpper = authority!!.uppercase()
            authoritiesFiltered = authoritiesFiltered
                .filter { r: RoleAuthority? -> r != null && r.authority.contains(authorityUpper) }
            allowNoRole = false
        }
        val authoritiesAllowed = authoritiesFiltered.toList()
        if (authoritiesAllowed.isEmpty()) {
            val builder = predicates.criteriaBuilder
            // never match
            predicates.add(builder!!.isTrue(builder.literal(false)))
            return
        }
        determineScope(predicates, roleJoin, query, authoritiesAllowed, allowNoRole)
    }

    private fun determineScope(
        predicates: PredicateBuilder,
        roleJoin: Join<User, Role>,
        query: CriteriaQuery<*>,
        authoritiesAllowed: List<RoleAuthority>,
        allowNoRole: Boolean
    ) {
        val authorityPredicates = predicates.newBuilder()
        var allowNoRoleInScope = allowNoRole
        // Is organization admin
        if (predicates.isValidValue(projectName)) {
            allowNoRoleInScope = false
            // Is project admin
            entitySubquery(
                RoleAuthority.Scope.PROJECT, roleJoin,
                query, authorityPredicates, authoritiesAllowed
            ) { b: PredicateBuilder?, proj: From<*, *> -> b!!.likeLower(proj.get("projectName"), projectName) }

            // Is organization admin for organization above current project
            if (isIncludeUpperLevels) {
                entitySubquery(
                    RoleAuthority.Scope.ORGANIZATION, roleJoin,
                    query, authorityPredicates, authoritiesAllowed
                ) { b: PredicateBuilder?, org: From<*, *> ->
                    b!!.likeLower(
                        org.join<Any, Any>("projects").get("projectName"), projectName
                    )
                }
            }
        } else if (predicates.isValidValue(organization)) {
            allowNoRoleInScope = false
            entitySubquery(
                RoleAuthority.Scope.ORGANIZATION, roleJoin,
                query, authorityPredicates, authoritiesAllowed
            ) { b: PredicateBuilder?, org: From<*, *> -> b!!.likeLower(org.get("name"), organization) }
        }
        if (authorityPredicates.isEmpty) {
            // no project or organization filters applied
            addAllowedAuthorities(authorityPredicates, roleJoin, authoritiesAllowed, null)
        } else if (isIncludeUpperLevels) {
            // is sys admin
            addAllowedAuthorities(
                authorityPredicates, roleJoin, authoritiesAllowed,
                RoleAuthority.Scope.GLOBAL
            )
        }
        if (allowNoRoleInScope) {
            authorityPredicates!!.isNull(roleJoin.get<Any>("id"))
        }
        predicates.add(authorityPredicates!!.toOrPredicate())
    }

    private fun addAllowedAuthorities(
        predicates: PredicateBuilder?,
        roleJoin: Join<User, Role>,
        authorities: List<RoleAuthority>,
        scope: RoleAuthority.Scope?
    ): Boolean {
        var authorityStream = authorities.stream()
        if (scope != null) {
            authorityStream = authorityStream.filter { r: RoleAuthority -> r.scope === scope }
        }
        val authorityNames = authorityStream
            .map(RoleAuthority::authority)
            .toList()
        return if (!authorityNames.isEmpty()) {
            predicates!!.`in`(roleJoin.get<Any>("authority").get<Any>("name"), authorityNames)
            true
        } else {
            false
        }
    }

    /** Create a subquery to filter the roles.  */
    private fun entitySubquery(
        scope: RoleAuthority.Scope,
        roleJoin: Join<User, Role>,
        query: CriteriaQuery<*>,
        predicates: PredicateBuilder?,
        allowedRoles: List<RoleAuthority>,
        queryMatch: BiConsumer<PredicateBuilder?, From<*, *>>
    ) {
        val authorityPredicates = predicates!!.newBuilder()
        if (!addAllowedAuthorities(authorityPredicates, roleJoin, allowedRoles, scope)) {
            return
        }
        val subQuery = query.subquery(Long::class.java)
        subQuery.distinct(true)
        val orgRoot = subQuery.from(
            when (scope) {
                RoleAuthority.Scope.PROJECT -> Project::class.java
                RoleAuthority.Scope.ORGANIZATION -> Organization::class.java
                else -> throw IllegalStateException("Unknown role scope $scope")
            } as Class<*>
        )
        subQuery.select(orgRoot.get("id"))
        val subqueryPredicates = predicates.newBuilder()
        queryMatch.accept(subqueryPredicates, orgRoot)
        subQuery.where(subqueryPredicates!!.toAndPredicate())
        authorityPredicates!!.`in`(
            roleJoin.get<Any>(
                when (scope) {
                    RoleAuthority.Scope.ORGANIZATION -> "organization"
                    RoleAuthority.Scope.PROJECT -> "project"
                    else -> throw IllegalStateException("Unknown role scope $scope")
                }
            ).get<Any>("id"), subQuery
        )
        predicates.add(authorityPredicates.toAndPredicate())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserFilter::class.java)
    }
}
