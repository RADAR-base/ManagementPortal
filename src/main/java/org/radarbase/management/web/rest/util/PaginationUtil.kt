package org.radarbase.management.web.rest.util

import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.web.rest.criteria.CriteriaRange
import org.radarbase.management.web.rest.criteria.SubjectAuthority
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.radarbase.management.web.rest.criteria.SubjectSortOrder
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder
import java.util.function.Consumer

/**
 * Utility class for handling pagination.
 *
 *
 *
 * Pagination uses the same principles as the
 * [GithubAPI](https://developer.github.com/v3/#pagination),
 * and follow [RFC 5988 (Link header)](http://tools.ietf.org/html/rfc5988).
 *
 */
object PaginationUtil {
    /**
     * Generate headers for pagination.
     * @param page the page
     * @param baseUrl the base URL
     * @return the [HttpHeaders]
     */
    fun generatePaginationHttpHeaders(page: Page<*>?, baseUrl: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-Total-Count", page!!.totalElements.toString())
        val link = StringBuilder(256)
        if (page.number + 1 < page.totalPages) {
            link.append('<')
                .append(generateUri(baseUrl, page.number + 1, page.size))
                .append(">; rel=\"next\",")
        }
        // prev link
        if (page.number > 0) {
            link.append('<')
                .append(generateUri(baseUrl, page.number - 1, page.size))
                .append(">; rel=\"prev\",")
        }
        // last and first link
        var lastPage = 0
        if (page.totalPages > 0) {
            lastPage = page.totalPages - 1
        }
        link.append('<')
            .append(generateUri(baseUrl, lastPage, page.size))
            .append(">; rel=\"last\",<")
            .append(generateUri(baseUrl, 0, page.size))
            .append(">; rel=\"first\"")
        headers.add(HttpHeaders.LINK, link.toString())
        return headers
    }

    /**
     * Generate pagination HTTP headers for subjects given a subject filter.
     * @param page the page
     * @param baseUrl the base URL
     * @param criteria subject criteria
     * @return the [HttpHeaders]
     */
    fun generateSubjectPaginationHttpHeaders(
        page: Page<SubjectDTO?>, baseUrl: String, criteria: SubjectCriteria
    ): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-Total-Count", page.totalElements.toString())
        if (!page.isEmpty) {
            val link = ('<'
                .toString() + generateUri(page, baseUrl, criteria)
                    + ">; rel=\"next\"")
            headers.add(HttpHeaders.LINK, link)
        }
        return headers
    }

    private fun generateUri(baseUrl: String, page: Int, size: Int): String {
        return UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString()
    }

    private fun generateUri(
        page: Page<SubjectDTO?>, baseUrl: String,
        criteria: SubjectCriteria
    ): String {
        val builder = UriComponentsBuilder.fromUriString(baseUrl)
        generateUriCriteriaRange(builder, "dateOfBirth", criteria.dateOfBirth)
        generateUriCriteriaRange(builder, "enrollmentDate", criteria.enrollmentDate)
        generateUriParam(builder, "externalId", criteria.externalId)
        generateUriParam(builder, "groupId", criteria.groupId)
        generateUriParam(builder, "personName", criteria.personName)
        generateUriParam(
            builder, "humanReadableIdentifier",
            criteria.humanReadableIdentifier
        )
        generateUriParam(builder, "projectName", criteria.projectName)
        generateUriParam(builder, "login", criteria.login)
        if (criteria.authority != null) {
            criteria.authority!!.forEach(Consumer { a: SubjectAuthority? ->
                generateUriParam(
                    builder,
                    "authority", a
                )
            })
        }
        generateUriParam(builder, "size", criteria.size)
        generateUriParam(builder, "page", criteria.page)
        if (criteria.getSort() != null) {
            criteria.getParsedSort()!!.forEach(Consumer { order: SubjectSortOrder? ->
                generateUriParam(
                    builder, "sort",
                    order?.sortBy?.queryParam + ','
                            + order?.direction?.name?.lowercase()
                )
            })
        }
        val lastSubject = page.content[page.numberOfElements - 1]
        generateUriParam(builder, "last.id", lastSubject?.id)
        generateUriParam(builder, "last.login", lastSubject?.login)
        if (lastSubject?.externalId != null && !lastSubject.externalId.isNullOrEmpty()) {
            generateUriParam(builder, "last.externalId", lastSubject.externalId)
        }
        return builder.toUriString()
    }

    private fun generateUriCriteriaRange(
        builder: UriComponentsBuilder, prefix: String,
        range: CriteriaRange<*>?
    ) {
        if (range == null) {
            return
        }
        generateUriParam(builder, "$prefix.is", range.iss)
        generateUriParam(builder, "$prefix.from", range.from)
        generateUriParam(builder, "$prefix.to", range.to)
    }

    private fun generateUriParam(
        builder: UriComponentsBuilder, name: String,
        value: Any?
    ) {
        if (value != null) {
            builder.queryParam(name, value)
        }
    }
}
