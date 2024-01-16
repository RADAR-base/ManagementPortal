package org.radarbase.management.security.jwt.algorithm

import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.jwks.JsonWebKey

/**
 * Encodes a signing and verification algorithm for JWT.
 */
interface JwtAlgorithm {
    /**
     * Auth0 Algorithm used in JWTs.
     */
    val algorithm: Algorithm

    /**
     * Encoded public key for storage or transmission.
     */
    val verifierKeyEncodedString: String

    /**
     * JavaWebKey for given algorithm for token verification.
     * @return instance of [JsonWebKey]
     */
    val jwk: JsonWebKey
}
