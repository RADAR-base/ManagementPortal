package org.radarcns.management.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for HTTP headers creation.
 */
public final class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private static final String APPLICATION_NAME = "managementPortalApp";

    private HeaderUtil() {
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-managementPortalApp-alert", message);
        headers.add("X-managementPortalApp-params", param);
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".created", param);
    }

    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".updated", param);
    }

    public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".deleted", param);
    }

    public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
        log.error("Entity creation failed, {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-managementPortalApp-error", "error." + errorKey);
        headers.add("X-managementPortalApp-params", entityName);
        return headers;
    }

    /**
     * URLEncode each component, prefix and join them by forward slashes.
     *
     * E.g. <code>buildPath("api", "projects", "radar/1")</code> results in the string
     * <code>/api/projects/radar%2F1</code>.
     *
     * @param components The components of the path.
     * @return A String where the components are URLEncoded and joined by forward slashes.
     */
    public static String buildPath(String... components) {
        return Arrays.stream(components)
                .filter(Objects::nonNull)
                .filter(c -> !c.isEmpty())
                .map(c -> {
                    // try-catch needs to be inside the lambda
                    try {
                        return URLEncoder.encode(c, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        log.error(ex.getMessage());
                        return "";
                    }
                })
                .reduce("", (a, b) -> String.join("/", a, b));
    }
}
