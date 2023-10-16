package org.radarbase.auth.token

import org.radarbase.auth.authorization.AuthorityReference
import java.time.Instant

/**
 * Created by dverbeec on 10/01/2018.
 */
interface RadarToken {
    /**
     * Get all roles defined in this token.
     * @return non-null set describing the roles defined in this token.
     */
    val roles: Set<AuthorityReference>

    /**
     * Get a list of scopes assigned to this token.
     * @return non-null list of scope names
     */
    val scopes: Set<String>

    /**
     * Get a list of source names associated with this token.
     * @return non-null list of source names
     */
    val sources: List<String>

    /**
     * Get this token's OAuth2 grant type.
     * @return grant type
     */
    val grantType: String?

    /**
     * Get the token subject.
     * @return non-null subject
     */
    val subject: String?

    /**
     * Get the token username.
     */
    val username: String?

    /**
     * Get the date this token was issued.
     * @return date this token was issued or null
     */
    val issuedAt: Instant?

    /**
     * Get the date this token expires.
     * @return date this token expires or null
     */
    val expiresAt: Instant

    /**
     * Get the audience of the token.
     * @return non-null list of resources that are allowed to accept the token
     */
    val audience: List<String>

    /**
     * Get a string representation of this token.
     * @return string representation of this token
     */
    val token: String?

    /**
     * Get the issuer.
     * @return issuer
     */
    val issuer: String?

    /**
     * Get the token type.
     * @return token type.
     */
    val type: String?

    /**
     * Client that the token is associated to.
     * @return client ID if set or null if unknown.
     */
    val clientId: String?

    /**
     * Whether the current credentials were obtained with a OAuth 2.0 client credentials flow.
     *
     * @return true if the client credentials flow was certainly used, false otherwise.
     */
    val isClientCredentials: Boolean
        get() = grantType == CLIENT_CREDENTIALS

    fun copyWithRoles(roles: Set<AuthorityReference>): RadarToken

    companion object {
        const val CLIENT_CREDENTIALS = "client_credentials"
    }
}
