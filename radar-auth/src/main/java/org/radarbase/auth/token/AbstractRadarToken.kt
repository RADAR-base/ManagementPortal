package org.radarbase.auth.token

/**
 * Partial implementation of [RadarToken], providing a default implementation for the three
 * permission checks.
 */
abstract class AbstractRadarToken : RadarToken {
    override val isClientCredentials: Boolean
        get() = CLIENT_CREDENTIALS == grantType

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other?.javaClass != javaClass) return false

        other as AbstractRadarToken
        return token == other.token
    }

    override fun hashCode(): Int = token.hashCode()

    override fun toString(): String = buildString {
        append("AbstractRadarToken{scopes=")
        append(scopes)
        append(", username='")
        append(username)
        append('\'')
        append(", subject='")
        append(subject)
        append('\'')
        append(", roles=")
        append(roles)
        append(", sources=")
        append(sources)
        append(", grantType='")
        append(grantType)
        append('\'')
        append(", audience=")
        append(audience)
        append(", issuer='")
        append(issuer)
        append('\'')
        append(", issuedAt=")
        append(issuedAt)
        append(", expiresAt=")
        append(expiresAt)
        append(", type='")
        append(type)
        append('\'')
        append('}')
    }

    companion object {
        const val CLIENT_CREDENTIALS = "client_credentials"
    }
}
