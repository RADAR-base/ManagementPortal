package org.radarbase.management.security.jwt.algorithm;

import com.auth0.jwt.algorithms.Algorithm;
import org.radarbase.auth.security.jwk.JavaWebKey;

/**
 * Encodes a signing and verification algorithm for JWT.
 */
public interface JwtAlgorithm {

    /**
     * Auth0 Algorithm used in JWTs.
     */
    Algorithm getAlgorithm();

    /**
     * Encoded public key for storage or transmission.
     */
    String getVerifierKeyEncodedString();

    /**
     * JavaWebKey for given algorithm for token verification.
     * @return instance of {@link JavaWebKey}
     */
    JavaWebKey getJwk();
}
