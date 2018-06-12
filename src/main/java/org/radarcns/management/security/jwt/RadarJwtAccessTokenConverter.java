package org.radarcns.management.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.util.Assert;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Customized implementation of {@link JwtAccessTokenConverter} for the RADAR-base platform.
 *
 * This class can accept an EC keypair as well as an RSA keypair for signing. EC signatures
 * are significantly smaller than RSA signatures.
 */
public class RadarJwtAccessTokenConverter extends JwtAccessTokenConverter {

    private Algorithm algorithm;

    @Override
    public void setKeyPair(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        Assert.state(privateKey instanceof RSAPrivateKey || privateKey instanceof ECPrivateKey,
                "KeyPair must be an RSA or EC keypair");
        if (privateKey instanceof ECPrivateKey) {
            algorithm = Algorithm.ECDSA256((ECPublicKey) keyPair.getPublic(),
                    (ECPrivateKey) keyPair.getPrivate());
            setSigner(new EcdsaSigner((ECPrivateKey) privateKey));
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            setVerifier(new EcdsaVerifier(publicKey));
            setVerifierKey("-----BEGIN EC PUBLIC KEY-----\n"
                    + new String(Base64.encode(publicKey.getEncoded()))
                    + "\n-----END EC PUBLIC KEY-----");
        } else {
            algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(),
                    (RSAPrivateKey) keyPair.getPrivate());
            setSigner(new RsaSigner((RSAPrivateKey) privateKey));
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            setVerifier(new RsaVerifier(publicKey));
            setVerifierKey("-----BEGIN PUBLIC KEY-----\n"
                    + new String(Base64.encode(publicKey.getEncoded()))
                    + "\n-----END PUBLIC KEY-----");
        }
    }

    protected String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        // we need to override the encode method as well, Spring security does not know about
        // ECDSA so it can not set the 'alg' header claim of the JWT to the correct value; here
        // we use the auth0 JWT implementation to create a signed, encoded JWT.
        Map<String, ?> claims = getAccessTokenConverter().convertAccessToken(accessToken,
                authentication);

        // create a builder and add the JWT defined claims
        JWTCreator.Builder builder = JWT.create();

        // add the string array claims
        Arrays.asList("aud", "sources", "roles", "authorities", "scope").stream()
                .filter(claims::containsKey)
                .forEach(claim ->
                builder.withArrayClaim(claim,
                        ((Collection<String>) claims.get(claim)).toArray(new String[] {})));

        // add the string claims
        Arrays.asList("sub", "iss", "user_name", "client_id", "grant_type", "jti", "ati").stream()
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim, (String) claims.get(claim)));

        // add the date claims, they are in seconds since epoch, we need milliseconds
        Arrays.asList("exp", "iat").stream()
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim, new Date(
                        ((Long) claims.get(claim)) * 1000)));

        String token = builder.sign(algorithm);
        return token;
    }

    @Override
    public boolean isPublic() {
        return true;
    }
}
