package org.radarbase.management.web.rest.util;

import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.web.rest.criteria.CriteriaRange;
import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import java.util.Locale;

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
            Page<SubjectDTO> page, String baseUrl, SubjectCriteria criteria
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        if (!page.isEmpty()) {
            String link = '<'
                    + generateUri(page, baseUrl, criteria)
                    + ">; rel=\"next\"";
            headers.add(HttpHeaders.LINK, link);
        }
        return headers;
    }

    private static String generateUri(String baseUrl, int page, int size) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }

    private static String generateUri(Page<SubjectDTO> page, String baseUrl,
            SubjectCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
        generateUriCriteriaRange(builder, "dateOfBirth", criteria.getDateOfBirth());
        generateUriCriteriaRange(builder, "enrollmentDate", criteria.getEnrollmentDate());
        generateUriParam(builder, "externalId", criteria.getExternalId());
        generateUriParam(builder, "groupId", criteria.getGroupId());
        generateUriParam(builder, "personName", criteria.getPersonName());
        generateUriParam(builder, "humanReadableIdentifier",
                criteria.getHumanReadableIdentifier());
        generateUriParam(builder, "projectName", criteria.getProjectName());
        generateUriParam(builder, "login", criteria.getLogin());
        if (criteria.getAuthority() != null) {
            criteria.getAuthority().forEach(a -> generateUriParam(builder,
                    "authority", a));
        }
        generateUriParam(builder, "size", criteria.getSize());
        generateUriParam(builder, "page", criteria.getPage());
        if (criteria.getSort() != null) {
            criteria.getParsedSort().forEach(order -> generateUriParam(builder, "sort",
                    order.getSortBy().getQueryParam() + ','
                            + order.getDirection().name().toLowerCase(Locale.ROOT)));
        }
        SubjectDTO lastSubject = page.getContent().get(page.getNumberOfElements() - 1);
        generateUriParam(builder, "last.id", lastSubject.getId());
        generateUriParam(builder, "last.login", lastSubject.getLogin());
        if (lastSubject.getExternalId() != null && !lastSubject.getExternalId().isEmpty()) {
            generateUriParam(builder, "last.externalId", lastSubject.getExternalId());
        }
        return builder.toUriString();
    }

    private static void generateUriCriteriaRange(UriComponentsBuilder builder, String prefix,
            CriteriaRange<?> range) {
        if (range == null) {
            return;
        }
        generateUriParam(builder, prefix + ".is", range.getIs());
        generateUriParam(builder, prefix + ".from", range.getFrom());
        generateUriParam(builder, prefix + ".to", range.getTo());
    }

    private static void generateUriParam(UriComponentsBuilder builder, String name,
            @Nullable Object value) {
        if (value != null) {
            builder.queryParam(name, value);
        }
    }
}
