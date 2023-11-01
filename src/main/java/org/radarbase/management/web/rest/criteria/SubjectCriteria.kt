package org.radarbase.management.web.rest.criteria

import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class SubjectCriteria {
    var authority = listOf(SubjectAuthority.ROLE_PARTICIPANT)
    var dateOfBirth: LocalDateCriteriaRange? = null
    var enrollmentDate: ZonedDateTimeCriteriaRange? = null
    var groupId: Long? = null
    var humanReadableIdentifier: String? = null
    var last: SubjectCriteriaLast? = null
    var page: @Min(0) Int = 0
    var size: @Min(1) Int = 20
    private var sort: List<String>? = null
    var personName: String? = null
    var projectName: String? = null
    var externalId: String? = null
    var login: String? = null

    @Transient
    private var parsedSort: List<SubjectSortOrder>? = null

    @get:NotNull
    val pageable: Pageable
        /** Get the criteria paging settings, excluding sorting.  */
        get() = PageRequest.of(page, size)

    fun getSort(): List<String>? {
        return sort
    }

    /** Parse the sort criteria.  */
    fun getParsedSort(): List<SubjectSortOrder>? {
        if (parsedSort == null) {
            val flatSort = if (sort != null) sort!!.stream()
                .flatMap { s: String ->
                    Arrays.stream(s.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                }
                .toList() else listOf()
            val parsedSort: MutableList<SubjectSortOrder?> = ArrayList(flatSort.size)
            var hasDirection = true
            var previous: SubjectSortOrder? = null
            for (part in flatSort) {
                if (!hasDirection) {
                    val direction = Sort.Direction.fromOptionalString(part)
                    if (direction.isPresent) {
                        previous?.direction = direction.get()
                        hasDirection = true
                        continue
                    }
                } else {
                    hasDirection = false
                }
                previous = SubjectSortOrder(getSubjectSortBy(part))
                parsedSort.add(previous)
            }
            optimizeSortList(parsedSort)
            this.parsedSort = Collections.unmodifiableList(parsedSort).filterNotNull()
        }
        return parsedSort
    }

    override fun toString(): String {
        return ("SubjectCriteria{" + "authority=" + authority
                + ", dateOfBirth=" + dateOfBirth
                + ", enrollmentDate=" + enrollmentDate
                + ", groupId='" + groupId + '\''
                + ", humanReadableIdentifier='" + humanReadableIdentifier + '\''
                + ", last=" + last
                + ", page=" + page
                + ", sort=" + sort
                + ", personName='" + personName + '\''
                + ", projectName='" + projectName + '\''
                + ", externalId='" + externalId + '\''
                + ", login='" + login + '\''
                + '}')
    }

    companion object {
        /**
         * Remove duplication and redundancy from sort list and make the result order consistent.
         * @param sort modifiable ordered sort collection.
         */
        private fun optimizeSortList(sort: MutableCollection<SubjectSortOrder?>) {
            val seenSortBy: EnumSet<SubjectSortBy>? = EnumSet.noneOf(
                SubjectSortBy::class.java
            )
            var hasUnique = false
            val iterator = sort.iterator()
            while (iterator.hasNext()) {
                val order = iterator.next()
                if (hasUnique || seenSortBy?.add(order?.sortBy) != true) {
                    iterator.remove()
                }
                if (order?.sortBy?.isUnique == true) {
                    hasUnique = true
                }
            }
            if (!hasUnique) {
                sort.add(SubjectSortOrder(SubjectSortBy.ID))
            }
        }

        private fun getSubjectSortBy(param: String): SubjectSortBy {
            return Arrays.stream<SubjectSortBy>(SubjectSortBy.values())
                .filter { s: SubjectSortBy -> s.queryParam.equals(param, ignoreCase = true) }
                .findAny()
                .orElseThrow<BadRequestException> {
                    BadRequestException(
                        "Cannot convert sort parameter " + param
                                + " to subject property", EntityName.Companion.SUBJECT, ErrorConstants.ERR_VALIDATION
                    )
                }
        }
    }
}
