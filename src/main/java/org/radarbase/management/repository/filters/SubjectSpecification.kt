package org.radarbase.management.repository.filters

import org.radarbase.management.domain.Role
import org.radarbase.management.domain.Subject
import org.radarbase.management.domain.User
import org.radarbase.management.web.rest.criteria.LocalDateCriteriaRange
import org.radarbase.management.web.rest.criteria.SubjectAuthority
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.radarbase.management.web.rest.criteria.SubjectCriteriaLast
import org.radarbase.management.web.rest.criteria.SubjectSortBy
import org.radarbase.management.web.rest.criteria.SubjectSortOrder
import org.radarbase.management.web.rest.criteria.ZonedDateTimeCriteriaRange
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Order
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


class SubjectSpecification(criteria: SubjectCriteria) : Specification<Subject?> {
    private val dateOfBirth: LocalDateCriteriaRange?
    private val enrollmentDate: ZonedDateTimeCriteriaRange?
    private val groupId: Long?
    private val humanReadableIdentifier: String?
    private val last: SubjectCriteriaLast?
    private val personName: String?
    private val projectName: String?
    private val externalId: String?
    private val subjectId: String?
    private val sort: List<SubjectSortOrder>?
    private val authority: Set<String>
    private var sortLastValues: List<String>? = null

    /**
     * Subject specification based on criteria.
     * @param criteria criteria to use for the specification.
     */
    init {
        authority = criteria.authority
            .map { obj: SubjectAuthority -> obj.name }
            .toSet()
        dateOfBirth = criteria.dateOfBirth
        enrollmentDate = criteria.enrollmentDate
        groupId = criteria.groupId
        humanReadableIdentifier = criteria.humanReadableIdentifier
        last = criteria.last
        personName = criteria.personName
        projectName = criteria.projectName
        externalId = criteria.externalId
        subjectId = criteria.login
        sort = criteria.parsedSort
        sortLastValues = if (last != null) {
            sort
                ?.mapNotNull { o: SubjectSortOrder -> getLastValue(o.sortBy) }
                ?.toList()
        } else {
            null
        }
    }

    override fun toPredicate(
        root: Root<Subject?>, query: CriteriaQuery<*>,
        builder: CriteriaBuilder
    ): Predicate? {
        if (root == null || query == null || builder == null) {
            return null;
        }
        query.distinct(true)
        root.alias("subject")
        val userJoin = root.join<Subject, User>("user")
        userJoin.alias("user")
        val predicates = PredicateBuilder(builder)
        addRolePredicates(userJoin, predicates)
        predicates.attributeLike(
            root, "Human-readable-identifier",
            humanReadableIdentifier
        )
        predicates.likeLower(root.get("externalId"), externalId)
        predicates.equal(root.get("group"), groupId)
        predicates.range(root.get("dateOfBirth"), dateOfBirth)
        predicates.range(root.get("enrollmentDate"), enrollmentDate)
        predicates.likeLower(root.get("personName"), personName)
        predicates.likeLower(userJoin.get("login"), subjectId)
        addContentPredicates(predicates, builder, root, query.resultType)
        query.orderBy(getSortOrder(root, builder))
        return predicates.toAndPredicate()!!
    }

    private fun filterLastValues(root: Root<Subject?>, builder: CriteriaBuilder): Predicate? {
        val lastPredicates = arrayOfNulls<Predicate?>(
            sort!!.size
        )
        val paths: MutableList<Path<String>> = ArrayList(
            sort.size
        )
        for (order in sort) {
            paths.add(getPropertyPath(order.sortBy, root))
        }
        for (i in sort.indices) {
            val lastAndPredicates: Array<Predicate?>? = if (i > 0) arrayOfNulls<Predicate>(i + 1) else null

            for (j in 0 until i) {
                lastAndPredicates!![j] = builder.equal(paths[j], sortLastValues!![j])
            }
            val order = sort[i]
            val saveVal = sortLastValues
                ?: throw BadRequestException(
                    "No last value given",
                    EntityName.SUBJECT, ErrorConstants.ERR_VALIDATION
                )
            val currentSort: Predicate? = if (order.direction.isAscending) {
                builder.greaterThan(paths[i], saveVal[i])
            } else {
                builder.lessThan(paths[i], saveVal[i])
            }
            if (lastAndPredicates != null) {
                lastAndPredicates[i] = currentSort
                lastPredicates[i] = builder.and(*lastAndPredicates)
            } else {
                lastPredicates[i] = currentSort
            }
        }
        return if (lastPredicates.size > 1) {
            builder.or(*lastPredicates)
        } else {
            lastPredicates[0]
        }
    }

    private fun addContentPredicates(
        predicates: PredicateBuilder, builder: CriteriaBuilder,
        root: Root<Subject?>, queryResult: Class<*>
    ) {
        // Don't add content for count queries.
        if (Long::class.javaObjectType == queryResult) {
            return
        }
        root.fetch<Any, Any>("sources", JoinType.LEFT)
        root.fetch<Any, Any>("user", JoinType.INNER)
        if (last != null) {
            predicates.add(filterLastValues(root, builder))
        }
    }

    private fun getLastValue(property: SubjectSortBy): String? {
        val result = when (property) {
            SubjectSortBy.ID -> last?.id
            SubjectSortBy.USER_LOGIN -> last?.login
            SubjectSortBy.EXTERNAL_ID -> last?.login
        }
        if (property.isUnique && result == null) {
            throw BadRequestException(
                "No last value given for sort property $property",
                EntityName.SUBJECT, ErrorConstants.ERR_VALIDATION
            )
        }
        return result
    }

    private fun getPropertyPath(property: SubjectSortBy, root: Root<Subject?>): Path<String> {
        return when (property) {
            SubjectSortBy.ID -> root.get("id")
            SubjectSortBy.USER_LOGIN -> root.get<Any>("user").get("login")
            SubjectSortBy.EXTERNAL_ID -> root.get("externalId")
        }
    }

    private fun addRolePredicates(userJoin: Join<Subject, User>, predicates: PredicateBuilder) {
        val rolesJoin = userJoin.join<User, Role>("roles")
        rolesJoin.alias("roles")
        predicates.equal({ rolesJoin.get<Any>("project").get("projectName") }, projectName)
        if (authority.isNotEmpty() && authority.size != SubjectAuthority.values().size) {
            predicates.add(rolesJoin.get<Any>("authority").get<Any>("name").`in`(authority))
        }
    }

    private fun getSortOrder(
        root: Root<Subject?>,
        builder: CriteriaBuilder
    ): List<Order>? {
        return sort
            ?.map { order: SubjectSortOrder ->
                val path = getPropertyPath(order.sortBy, root)
                if (order.direction.isAscending) {
                    return listOf(builder.asc(path))
                } else {
                    return listOf(builder.desc(path))
                }
            }
            ?.toList()
    }
}
