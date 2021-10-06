package org.radarbase.management.web.rest.util;

import org.radarbase.management.repository.filters.SubjectFilter;
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
    
    public static HttpHeaders generateSubjectPaginationHttpHeaders(
        Page<?> page, String baseUrl, SubjectFilter filter
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        StringBuilder link = new StringBuilder(256);
        link.append('<')
                .append(generateUri(baseUrl, filter))
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

    private static String generateUri(String baseUrl, SubjectFilter filter) {
        return UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("externalId", filter.getExternalId())
            .queryParam("groupName", filter.getGroupName())
            .queryParam("lastLoadedId", filter.getLastLoadedId())
            .queryParam("pageSize", filter.getPageSize())
            .queryParam("projectName", filter.getProjectName())
            .queryParam("sortBy", filter.getSortBy().getKey())
            .queryParam("sortDirection", filter.getSortDirection().getKey())
            .queryParam("subjectId", filter.getSubjectId())
            .queryParam("withInactiveParticipants", filter.getWithInactiveParticipants())
            .toUriString();
    }
}
