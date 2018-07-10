package org.radarcns.auth.token.validation;

import com.auth0.jwt.algorithms.Algorithm;

public interface TokenValidationAlgorithm {
    /**
     * Get the algorithm description as it will be reported by the server public key endpoint
     * (e.g. "SHA256withRSA" or "SHA256withEC").
     * @return the algorithm description
     */
    String getJwtAlgorithm();

    /**
     * Get the header for a PEM encoded key that this algorithm can parse.
     *
     * @return the header for a PEM encoded key that this algorithm can parse
     */
    String getKeyHeader();

    /**
     * Build a verification algorithm based on the supplied public key.
     * @param publicKey the public key in PEM format
     * @return the verification algorithm
     */
    Algorithm getAlgorithm(String publicKey);
}
