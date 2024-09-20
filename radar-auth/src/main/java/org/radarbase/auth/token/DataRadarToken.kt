package org.radarbase.auth.token

import org.radarbase.auth.authorization.AuthorityReference
import java.io.Serializable
import java.time.Instant

/**
 * Created by dverbeec on 10/01/2018.
 */
data class DataRadarToken(
    /**
     * Get all roles defined in this token.
     * @return non-null set describing the roles defined in this token.
     */
    override val roles: Set<AuthorityReference>,
    /**
     * Get a list of scopes assigned to this token.
     * @return non-null list of scope names
     */
    override val scopes: Set<String>,
    /**
     * Get a list of source names associated with this token.
     * @return non-null list of source names
     */
    override val sources: List<String> = emptyList(),
    /**
     * Get this token's OAuth2 grant type.
     * @return grant type
     */
    override val grantType: String?,
    /**
     * Get the token subject.
     * @return non-null subject
     */
    override val subject: String? = null,
    /**
     * Get the token username.
     */
    override val username: String? = null,
    /**
     * Get the date this token was issued.
     * @return date this token was issued or null
     */
    override val issuedAt: Instant? = null,
    /**
     * Get the date this token expires.
     * @return date this token expires or null
     */
    override val expiresAt: Instant,
    /**
     * Get the audience of the token.
     * @return non-null list of resources that are allowed to accept the token
     */
    override val audience: List<String> = listOf(),
    /**
     * Get a string representation of this token.
     * @return string representation of this token
     */
    override val token: String? = null,
    /**
     * Get the issuer.
     * @return issuer
     */
    override val issuer: String? = null,
    /**
     * Get the token type.
     * @return token type.
     */
    override val type: String? = null,
    /**
     * the authenticator assurance level of the token
     * @return default.
     */
    override val authenticatorAssuranceLevel: RadarToken.AuthenticatorAssuranceLevel = RadarToken.AuthenticatorAssuranceLevel.AAL1,
    /**
     * Client that the token is associated to.
     * @return client ID if set or null if unknown.
     */
    override val clientId: String? = null,
) : RadarToken,
    Serializable {
        constructor(radarToken: RadarToken) : this(
            roles = radarToken.roles,
            scopes = radarToken.scopes,
            sources = radarToken.sources,
            grantType = radarToken.grantType,
            subject = radarToken.subject,
            username = radarToken.username,
            issuedAt = radarToken.issuedAt,
            expiresAt = radarToken.expiresAt,
            audience = radarToken.audience,
            token = radarToken.token,
            issuer = radarToken.issuer,
            type = radarToken.type,
            clientId = radarToken.clientId,
        )

        override fun copyWithRoles(roles: Set<AuthorityReference>): DataRadarToken = copy(roles = roles)

        companion object {
            fun RadarToken.toDataRadarToken(): DataRadarToken = DataRadarToken(this)
        }
    }
