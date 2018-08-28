package org.radarcns.management.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * Customized implementation of {@link JwtAccessTokenConverter} for the RADAR-base platform.
 *
 * <p>This class can accept an EC keypair as well as an RSA keypair for signing. EC signatures
 * are significantly smaller than RSA signatures.</p>
 */
public class RadarJwtAccessTokenConverter extends JwtAccessTokenConverter {
    private static final Logger logger = LoggerFactory
            .getLogger(RadarJwtAccessTokenConverter.class);

    private Algorithm algorithm;

    /**
     * Default constructor.
     * Creates {@link RadarJwtAccessTokenConverter} with {@link DefaultAccessTokenConverter} as
     * the accessTokenConverter with explicitly including grant_type claim.
     */
    public RadarJwtAccessTokenConverter() {
        DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
        tokenConverter.setIncludeGrantType(true);
        setAccessTokenConverter(tokenConverter);
    }

    @Override
    public void setKeyPair(KeyPair keyPair) {
        JwtAlgorithm alg = getJwtAlgorithm(keyPair);
        if (alg == null) {
            throw new IllegalArgumentException("KeyPair type "
                    + keyPair.getPrivate().getAlgorithm() + " is unknown.");
        }
        algorithm = alg.getAlgorithm();
        setSigner(alg.getSigner());
        setVerifier(alg.getVerifier());
        setVerifierKey(alg.getEncodedString());
    }

    @Override
    protected String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        // we need to override the encode method as well, Spring security does not know about
        // ECDSA so it can not set the 'alg' header claim of the JWT to the correct value; here
        // we use the auth0 JWT implementation to create a signed, encoded JWT.
        Map<String, ?> claims = getAccessTokenConverter()
                .convertAccessToken(accessToken, authentication);

        // create a builder and add the JWT defined claims
        JWTCreator.Builder builder = JWT.create();

        // add the string array claims
        Stream.of("aud", "sources", "roles", "authorities", "scope")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withArrayClaim(claim,
                        ((Collection<String>) claims.get(claim)).toArray(new String[0])));

        // add the string claims
        Stream.of("sub", "iss", "user_name", "client_id", "grant_type", "jti", "ati")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim, (String) claims.get(claim)));

        // add the date claims, they are in seconds since epoch, we need milliseconds
        Stream.of("exp", "iat")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim, new Date(
                        ((Long) claims.get(claim)) * 1000)));

        return builder.sign(algorithm);
    }

    /**
     * Get the JWT algorithm to sign or verify JWTs with.
     * @param keyPair key pair for signing/verifying.
     * @return algorithm or {@code null} if the key type is unknown.
     */
    public static @Nullable JwtAlgorithm getJwtAlgorithm(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();

        if (privateKey instanceof ECPrivateKey) {
            return new EcdsaJwtAlgorithm(keyPair);
        } else if (privateKey instanceof RSAPrivateKey) {
            return new RsaJwtAlgorithm(keyPair);
        } else {
            logger.warn("No JWT algorithm found for key type {}", privateKey.getClass());
            return null;
        }
    }

    @Override
    public boolean isPublic() {
        String alg = getKey().getOrDefault("alg", "");
        // the signer is private in our superclass, but we can check the algorithm with getKey()
        return alg.equals("SHA256withECDSA") || alg.equals("SHA256withRSA");
    }
}
