package org.radarbase.auth.jwt

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory

class JwtTokenVerifier(
    private val algorithm: String,
    private val verifier: JWTVerifier,
) : TokenVerifier {
    override fun verify(token: String): RadarToken = try {
        val jwt = verifier.verify(token)

        // Do not print full token with signature to avoid exposing valid token in logs.
        logger.debug("Verified JWT header {} and payload {}", jwt.header, jwt.payload)

        JwtRadarToken(jwt)
    } catch (ex: Throwable) {
        when (ex) {
            is SignatureVerificationException -> logger.debug("Client presented a token with an incorrect signature.")
            is JWTVerificationException -> logger.debug("Verifier {} did not accept token: {}", verifier.javaClass, ex.message)
        }
        throw ex
    }

    override fun toString(): String = "JwtTokenVerifier(algorithm=$algorithm)"

    companion object {
        private val logger = LoggerFactory.getLogger(JwtTokenVerifier::class.java)
    }
}
