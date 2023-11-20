package org.radarbase.auth.kratos
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.exception.InsufficientAuthenticationLevelException
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory

//TODO How to get initial access (i.e. admin account), how to regain access if lost 2fa credentials for admin, (backdoor?)
//TODO Better error screen for no AAL2
//TODO Remove old login --> update unit tests, Testing kratos
class KratosTokenVerifier(private val sessionService: SessionService) : TokenVerifier {
    @Throws(IdpException::class)
    override suspend fun verify(token: String): RadarToken = try {
        val kratosSession = sessionService.getSession(token)

        val radarToken =  kratosSession.toDataRadarToken()
        if (radarToken.authenticatorLevel != RadarToken.AuthenticatorLevel.aal2)
        {
            val msg = "found a token of with aal: ${radarToken.authenticatorLevel}, which is insufficient for this" +
                " action"
            throw InsufficientAuthenticationLevelException(msg)
        }
        radarToken
    } catch (ex: InsufficientAuthenticationLevelException) {
        logger.warn(ex.message, ex)
        throw ex
    } catch (ex: Throwable) {
        logger.warn(ex.message, ex)
        throw IdpException("could not verify token", ex)
    }

    override fun toString(): String = "org.radarbase.auth.kratos.KratosTokenVerifier"

    companion object {
        private val logger = LoggerFactory.getLogger(KratosTokenVerifier::class.java)
    }
}
