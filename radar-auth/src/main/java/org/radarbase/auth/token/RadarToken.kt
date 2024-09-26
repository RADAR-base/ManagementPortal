package org.radarbase.auth.token

import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.token.RadarToken.AuthenticatorAssuranceLevel.aal1
import org.radarbase.auth.token.RadarToken.AuthenticatorAssuranceLevel.aal2
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
     * the authenticator assurance level of the token
     * @return default.
     */
    val authenticatorAssuranceLevel: AuthenticatorAssuranceLevel

    /**
     * Whether the current credentials were obtained with a OAuth 2.0 client credentials flow.
     *
     * @return true if the client credentials flow was certainly used, false otherwise.
     */
    val isClientCredentials: Boolean
        get() = grantType == CLIENT_CREDENTIALS || (subject != null && subject == clientId)

    fun copyWithRoles(roles: Set<AuthorityReference>): RadarToken

    companion object {
        const val CLIENT_CREDENTIALS = "client_credentials"
    }

    /**
     * Authenticator assurance level, commonly referred to as MFA. AAL1 means no MFA, AAL2 means MFA
     *
     * @property aal1 Represents the first level of authenticator assurance (e.g. password based).
     * @property aal2 Represents the second level of authenticator assurance (i.e. MFA).
     */
    enum class AuthenticatorAssuranceLevel {
        aal1,
        aal2
    }
}
