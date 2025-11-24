package org.radarbase.management.service

import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.springframework.security.oauth2.provider.ClientDetails
import java.util.*

/**
 * Utility class for OAuth client operations.
 */
object OAuthClientUtils {
    private const val PROTECTED_KEY = "protected"

    /**
     * Checks whether a client is a protected client.
     *
     * @param details ClientDetails.
     * @throws InvalidRequestException if the client is protected
     */
    fun checkProtected(details: ClientDetails) {
        val info = details.additionalInformation
        if (Objects.nonNull(info) && info.containsKey(PROTECTED_KEY) && info[PROTECTED_KEY]
                .toString().equals("true", ignoreCase = true)
        ) {
            throw InvalidRequestException(
                "Cannot modify protected client", EntityName.Companion.OAUTH_CLIENT,
                ErrorConstants.ERR_OAUTH_CLIENT_PROTECTED,
                Collections.singletonMap<String, String?>("client_id", details.clientId)
            )
        }
    }
}
