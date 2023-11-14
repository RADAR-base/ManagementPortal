package org.radarbase.auth.kratos
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory

class KratosTokenVerifier(private val sessionService: SessionService) : TokenVerifier {
    @Throws(IdpException::class)
    override suspend fun verify(token: String): RadarToken = try {
        val kratosSession = sessionService.getSession(token)

        DataRadarToken(
            roles = kratosSession.identity.parseRoles(),
            scopes = kratosSession.identity.metadata_public?.scope?.toSet() ?: emptySet(),
            sources = kratosSession.identity.metadata_public?.sources ?: emptyList(),
            grantType = "session",
            subject = kratosSession.identity.id,
            issuedAt = kratosSession.issued_at,
            expiresAt = kratosSession.expires_at,
            audience = kratosSession.identity.metadata_public?.aud ?: emptyList(),
            token = token,
            issuer = kratosSession.authentication_methods.first().provider,
            type = "type",
            clientId = "kratosSession",
            username = kratosSession.identity.metadata_public?.mp_login
        )

    } catch (ex: Throwable) {
        throw IdpException("could not verify token", ex)
    }

    override fun toString(): String = "org.radarbase.auth.kratos.KratosTokenVerifier"

    companion object {
        private val logger = LoggerFactory.getLogger(KratosTokenVerifier::class.java)
    }
}
