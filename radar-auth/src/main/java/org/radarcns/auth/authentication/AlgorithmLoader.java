package org.radarcns.auth.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.security.jwk.JavaWebKey;
import org.radarcns.auth.security.jwk.JavaWebKeySet;
import org.radarcns.auth.token.validation.ECTokenValidationAlgorithm;
import org.radarcns.auth.token.validation.RSATokenValidationAlgorithm;
import org.radarcns.auth.token.validation.TokenValidationAlgorithm;
import org.radarcns.auth.token.validation.deprecated.DeprecatedEcTokenValidationAlgorithm;

public class AlgorithmLoader {

    private static final List<TokenValidationAlgorithm> algorithmList = Arrays.asList(
            new ECTokenValidationAlgorithm(),
            new RSATokenValidationAlgorithm());
    private static final List<TokenValidationAlgorithm> supportedAlgorithmsForPublicKeys =
            Arrays.asList(
            new DeprecatedEcTokenValidationAlgorithm(),
            new RSATokenValidationAlgorithm());

    private static Algorithm algorithmFromPublicKey(String publicKey) {
        // We deny to trust the public key if the reported algorithm is unknown to us
        // https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
        return loadAlgorithmFromPublicKey(algorithmList, publicKey);
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
     * Loads algorithms using deprecated method.
     * @param publicKey publicKeys to load algorithms.
     * @return instance of {@link Algorithm}.
     */
    public static Algorithm loadDeprecatedAlgorithmFromPublicKey(String publicKey) {
        // We deny to trust the public key if the reported algorithm is unknown to us
        // https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
        return loadAlgorithmFromPublicKey(supportedAlgorithmsForPublicKeys, publicKey);
    }

    /**
     * Loads Algorithms from {@link JavaWebKeySet}.
     * @param publicKeyInfo java web key set to load algorithm.
     * @return List of {@link Algorithm}.
     */
    public static List<Algorithm> loadAlgorithmsFromJavaWebKeys(JavaWebKeySet publicKeyInfo) {
        return publicKeyInfo
                .getKeys()
                .stream()
                .map(JavaWebKey::getValue)
                .map(AlgorithmLoader::algorithmFromPublicKey)
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
}
