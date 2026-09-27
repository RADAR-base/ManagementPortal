package org.radarbase.management.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * URL at which ManagementPortal can reach its own endpoints from within its own process or
 * container. When the internal auth server is used, ManagementPortal calls its own
 * `/oauth/token` and `/oauth/token_key` endpoints. The public
 * [ManagementPortalProperties.Common.managementPortalBaseUrl] is not necessarily resolvable from
 * inside the container (e.g. `http://localhost/managementportal` behind an ingress), so these
 * calls use the local server port and context path instead.
 */
@Component
class InternalServerUrl(
    @Value("\${server.port:8080}") port: Int,
    @Value("\${server.servlet.context-path:}") contextPath: String,
) {
    val baseUrl: String = "http://localhost:$port${contextPath.trimEnd('/')}"
}
