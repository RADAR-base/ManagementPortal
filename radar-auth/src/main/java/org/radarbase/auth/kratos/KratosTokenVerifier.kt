package org.radarbase.auth.kratos
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.exception.InsufficientAuthenticationLevelException
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory

//TODO Better error screen for no AAL2
class KratosTokenVerifier(private val sessionService: SessionService, private val requireAal2: Boolean) : TokenVerifier {
    @Throws(IdpException::class)
    override suspend fun verify(token: String): RadarToken = try {
        val kratosSession = sessionService.getSession(token)

        val radarToken =  kratosSession.toDataRadarToken()
        if (radarToken.authenticatorAssuranceLevel != RadarToken.AuthenticatorAssuranceLevel.aal2 && requireAal2)
        {
            val msg = "found a token of with aal: ${radarToken.authenticatorAssuranceLevel}, which is insufficient for this" +
                " action"
            throw InsufficientAuthenticationLevelException(msg)
        }
        radarToken
    } catch (ex: InsufficientAuthenticationLevelException) {
        throw ex
    } catch (ex: Throwable) {
        throw IdpException("could not verify token", ex)
    }

    override fun toString(): String = "org.radarbase.auth.kratos.KratosTokenVerifier"

    companion object {
        private val logger = LoggerFactory.getLogger(KratosTokenVerifier::class.java)
    }
}
