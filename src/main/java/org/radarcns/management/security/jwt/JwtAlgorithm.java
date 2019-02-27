package org.radarcns.management.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;

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
}
