package org.radarbase.management.web.rest.util;

import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility class for handling pagination.
 *
 * <p>
 *     Pagination uses the same principles as the
 *     <a href="https://developer.github.com/v3/#pagination">GithubAPI</a>,
 *     and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

    /**
     * Generate headers for pagination.
     * @param page the page
     * @param baseUrl the base URL
     * @return the {@link HttpHeaders}
     */
    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page, String baseUrl) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));

        StringBuilder link = new StringBuilder(256);
        if ((page.getNumber() + 1) < page.getTotalPages()) {
            link.append('<')
                    .append(generateUri(baseUrl, page.getNumber() + 1, page.getSize()))
                    .append(">; rel=\"next\",");
        }
        // prev link
        if ((page.getNumber()) > 0) {
            link.append('<')
                    .append(generateUri(baseUrl, page.getNumber() - 1, page.getSize()))
                    .append(">; rel=\"prev\",");
        }
        // last and first link
        int lastPage = 0;
        if (page.getTotalPages() > 0) {
            lastPage = page.getTotalPages() - 1;
        }
        link.append('<')
                .append(generateUri(baseUrl, lastPage, page.getSize()))
                .append(">; rel=\"last\",<")
                .append(generateUri(baseUrl, 0, page.getSize()))
                .append(">; rel=\"first\"");
        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    /**
     * Generate pagination HTTP headers for subjects given a subject filter.
     * @param page the page
     * @param baseUrl the base URL
     * @param criteria subject criteria
     * @return the {@link HttpHeaders}
     */
    public static HttpHeaders generateSubjectPaginationHttpHeaders(
            Page<?> page, String baseUrl, SubjectCriteria criteria
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        StringBuilder link = new StringBuilder(256);
        link.append('<')
                .append(generateUri(baseUrl, criteria))
                .append(">; rel=\"first\"");
        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    private static String generateUri(String baseUrl, int page, int size) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }

    private static String generateUri(String baseUrl, SubjectCriteria criteria) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("dateOfBirthFrom", criteria.getDateOfBirthFrom())
                .queryParam("dateOfBirthTo", criteria.getDateOfBirthTo())
                .queryParam("enrollmentDateFrom", criteria.getEnrollmentDateFrom())
                .queryParam("enrollmentDateTo", criteria.getEnrollmentDateTo())
                .queryParam("externalId", criteria.getExternalId())
                .queryParam("groupName", criteria.getGroupName())
                .queryParam("lastLoadedId", criteria.getLastLoadedId())
                .queryParam("pageSize", criteria.getPageSize())
                .queryParam("personName", criteria.getPersonName())
                .queryParam("projectName", criteria.getProjectName())
                .queryParam("sortBy", criteria.getSortBy().getKey())
                .queryParam("sortDirection", criteria.getSortDirection().getKey())
                .queryParam("subjectId", criteria.getSubjectId())
                .queryParam("includeInactive", criteria.isIncludeInactive())
                .toUriString();
    }
}
