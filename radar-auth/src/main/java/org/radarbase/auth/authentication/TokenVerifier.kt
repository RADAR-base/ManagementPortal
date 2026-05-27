package org.radarbase.auth.authentication

import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken

/**
 * Verifies a token from string and returns a parsed version of it.
 */
interface TokenVerifier {
    /**
     * Verifies a token from string and returns a parsed version of it.
     * @throws TokenValidationException if the token cannot be verified by this verifier.
     */
    @Throws(TokenValidationException::class)
    suspend fun verify(token: String): RadarToken
}
