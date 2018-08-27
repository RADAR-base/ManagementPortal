package org.radarcns.management.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;

/**
 * Encodes a signing and verification algorithm for JWT.
 */
public interface JwtAlgorithm {
    /**
     * Signer to sign JWTs with.
     */
    Signer getSigner();

    /**
     * Verifier to verify JWTs with.
     */
    SignatureVerifier getVerifier();

    /**
     * Auth0 Algorithm used in JWTs.
     */
    Algorithm getAlgorithm();

    /**
     * Encoded public key for storage or transmission.
     */
    String getEncodedString();
}
