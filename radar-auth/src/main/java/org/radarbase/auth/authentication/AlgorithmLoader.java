package org.radarbase.auth.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.security.jwk.JavaWebKey;
import org.radarbase.auth.security.jwk.JavaWebKeySet;
import org.radarbase.auth.token.validation.ECTokenValidationAlgorithm;
import org.radarbase.auth.token.validation.RSATokenValidationAlgorithm;
import org.radarbase.auth.token.validation.TokenValidationAlgorithm;

public class AlgorithmLoader {

    private final List<TokenValidationAlgorithm> supportedAlgorithmsForWebKeySets;

    /**
     * Creates an instance of {@link AlgorithmLoader} with lists of
     * {@link TokenValidationAlgorithm} provided.
     * @param supportedAlgorithmsForWebKeySets default support. Algorithms to be supported for
     *                             public keys shared from public key endpoints as
     *                             {@link JavaWebKeySet}.
     */
    public AlgorithmLoader(
            List<TokenValidationAlgorithm> supportedAlgorithmsForWebKeySets) {
        this.supportedAlgorithmsForWebKeySets = supportedAlgorithmsForWebKeySets;
    }

    private Algorithm algorithmFromPublicKey(String publicKey) {
        // We deny to trust the public key if the reported algorithm is unknown to us
        // https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
        return loadAlgorithmFromPublicKey(supportedAlgorithmsForWebKeySets, publicKey);
    }

    private static Algorithm loadAlgorithmFromPublicKey(
            List<TokenValidationAlgorithm> supportedAlgorithms, String publicKey) {
        return supportedAlgorithms
                .stream()
                .filter(algorithm -> publicKey.startsWith(algorithm.getKeyHeader()))
                .findFirst()
                .orElseThrow(() ->
                        new TokenValidationException("Unsupported public key: " + publicKey))
                .getAlgorithm(publicKey);
    }

    /**
     * Loads Algorithms from {@link JavaWebKeySet}.
     * @param publicKeyInfo java web key set to load algorithm.
     * @return List of {@link Algorithm}.
     */
    public List<Algorithm> loadAlgorithmsFromJavaWebKeys(JavaWebKeySet publicKeyInfo) {
        return publicKeyInfo
                .getKeys()
                .stream()
                .map(JavaWebKey::getValue)
                .map(this::algorithmFromPublicKey)
                .collect(Collectors.toList());
    }

    /**
     * Creates in {@link JWTVerifier} using Algorithms and audience.
     * @param algorithm instance of {@link Algorithm} to create verifier from.
     * @param audience to which audience.
     * @return instance of {@link JWTVerifier}.
     */
    public static JWTVerifier buildVerifier(Algorithm algorithm, String audience) {
        return JWT.require(algorithm)
                .withAudience(audience)
                .build();
    }

    /** Load the default algorithms, ECDSA and RSA. */
    public static List<TokenValidationAlgorithm> defaultAlgorithms() {
        return Arrays.asList(
                new ECTokenValidationAlgorithm(),
                new RSATokenValidationAlgorithm());
    }
}
