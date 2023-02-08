package org.radarbase.auth.token

import java.util.*

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
     * @return non-null grant type
     */
    val grantType: String

    /**
     * Get the token subject.
     * @return non-null subject
     */
    val subject: String

    /**
     * Get the token username.
     */
    val username: String

    /**
     * Get the date this token was issued.
     * @return date this token was issued or null
     */
    val issuedAt: Date

    /**
     * Get the date this token expires.
     * @return date this token expires or null
     */
    val expiresAt: Date

    /**
     * Get the audience of the token.
     * @return non-null list of resources that are allowed to accept the token
     */
    val audience: List<String>

    /**
     * Get a string representation of this token.
     * @return non-null string representation of this token
     */
    val token: String

    /**
     * Get the issuer.
     * @return non-null issuer
     */
    val issuer: String

    /**
     * Get the token type.
     * @return non-null token type.
     */
    val type: String

    /**
     * Client that the token is associated to.
     * @return client ID if set or null if unknown.
     */
    val clientId: String

    /**
     * Get a token claim by name.
     * @param name claim name.
     * @return a claim value or null if none was found or the type was not a string.
     */
    fun getClaimString(name: String): String?

    /**
     * Get a token claim list by name.
     * @param name claim name.
     * @return a claim list of values or null if none was found or the type was not a string.
     */
    fun getClaimList(name: String): List<String>

    /**
     * Whether the current credentials were obtained with a OAuth 2.0 client credentials flow.
     *
     * @return true if the client credentials flow was certainly used, false otherwise.
     */
    val isClientCredentials: Boolean
}
