package org.radarbase.management.web.rest.util

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Utility class for HTTP headers creation.
 */
object HeaderUtil {
    private val log = LoggerFactory.getLogger(HeaderUtil::class.java)
    private const val APPLICATION_NAME = "managementPortalApp"

    /**
     * Create the headers for displaying an alert in the frontend.
     * @param message the message
     * @param param the message parameters
     * @return the [HttpHeaders]
     */
    fun createAlert(message: String?, param: String?): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-managementPortalApp-alert", message)
        headers.add("X-managementPortalApp-params", param)
        return headers
    }

    fun createEntityCreationAlert(entityName: String, param: String?): HttpHeaders {
        return createAlert(APPLICATION_NAME + "." + entityName + ".created", param)
    }

    fun createEntityUpdateAlert(entityName: String, param: String?): HttpHeaders {
        return createAlert(APPLICATION_NAME + "." + entityName + ".updated", param)
    }

    fun createEntityDeletionAlert(entityName: String, param: String?): HttpHeaders {
        return createAlert(APPLICATION_NAME + "." + entityName + ".deleted", param)
    }

    /**
     * Create headers to display a failure alert in the frontend.
     * @param entityName the entity on which the failure occurred
     * @param errorKey the error key in the translation dictionary
     * @param defaultMessage the default message
     * @return the [HttpHeaders]
     */
    fun createFailureAlert(
        entityName: String, errorKey: String,
        defaultMessage: String?
    ): HttpHeaders {
        log.error("Entity creation failed, {}", defaultMessage)
        val headers = HttpHeaders()
        headers.add("X-managementPortalApp-error", "error.$errorKey")
        headers.add("X-managementPortalApp-params", entityName)
        return headers
    }

    /**
     * Create headers to display a failure alert in the frontend.
     * @param entityName the entity on which the failure occurred
     * @param errorKey the error key in the translation dictionary
     * @param defaultMessage the default message
     * @return the [HttpHeaders]
     */
    fun createExceptionAlert(
        entityName: String?, errorKey: String?,
        defaultMessage: String?
    ): HttpHeaders {
        //TODO: Replace createFailureAlert with error. addition
        log.error("Entity creation failed, {}", defaultMessage)
        val headers = HttpHeaders()
        headers.add("X-managementPortalApp-error", errorKey)
        headers.add("X-managementPortalApp-params", entityName)
        return headers
    }

    /**
     * URLEncode each component, prefix and join them by forward slashes.
     *
     *
     * E.g. `buildPath("api", "projects", "radar/1")` results in the string
     * `/api/projects/radar%2F1`.
     *
     * @param components The components of the path.
     * @return A String where the components are URLEncoded and joined by forward slashes.
     */
    fun buildPath(vararg components: String?): String {
        return "/" + components
            .filterNotNull()
            .filter { it != "" }
            .map { c: String? ->
                // try-catch needs to be inside the lambda
                try {
                    return@map URLEncoder.encode(c, "UTF-8")
                } catch (ex: UnsupportedEncodingException) {
                    log.error(ex.message)
                    return@map ""
                }
            }
            .reduce { a: String, b: String -> java.lang.String.join("/", a, b) }
    }
}
