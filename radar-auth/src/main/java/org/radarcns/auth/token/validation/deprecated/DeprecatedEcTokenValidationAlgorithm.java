package org.radarcns.auth.token.validation.deprecated;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.exception.ConfigurationException;
import org.radarcns.auth.token.validation.AbstractTokenValidationAlgorithm;
import shadow.auth0.jwt.exceptions.JWTDecodeException;
import shadow.auth0.jwt.interfaces.Claim;

/**
 * This is a deprecated algorithm to validate old tokens.
 * Added to maintain compatibility with java-jwt:3.2.0 validators.
 *
 */
@Deprecated
public class DeprecatedEcTokenValidationAlgorithm extends AbstractTokenValidationAlgorithm {

    @Override
    public String getJwtAlgorithm() {
        return "SHA256withECDSA";
    }

    @Override
    public String getKeyHeader() {
        return "-----BEGIN EC PUBLIC KEY-----";
    }

    @Override
    /**
     * Parse a public key in PEM format.
     * @param publicKey the public key to parse
     * @return a PublicKey object representing the supplied public key
     */
    protected PublicKey parseKey(String publicKey) {
        try (PemReader pemReader = new PemReader(new StringReader(publicKey))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(getKeyFactoryType());
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public Algorithm getAlgorithm(String publicKey) {

        shadow.auth0.jwt.algorithms.Algorithm deprecatedEcVerifier =
                shadow.auth0.jwt.algorithms.Algorithm.ECDSA256((ECPublicKey) parseKey(publicKey),
                null);
        return new Algorithm(deprecatedEcVerifier.getName(), getJwtAlgorithm()) {
            @Override
            public void verify(DecodedJWT jwt) throws SignatureVerificationException {
                deprecatedEcVerifier.verify(wrap(jwt));
            }

            @Override
            public byte[] sign(byte[] contentBytes) throws SignatureGenerationException {
                return deprecatedEcVerifier.sign(contentBytes);
            }
        };
    }

    private shadow.auth0.jwt.interfaces.DecodedJWT wrap(DecodedJWT jwt) {
        return new shadow.auth0.jwt.interfaces.DecodedJWT() {
            @Override
            public String getToken() {
                return jwt.getToken();
            }

            @Override
            public String getHeader() {
                return jwt.getHeader();
            }

            @Override
            public String getPayload() {
                return jwt.getPayload();
            }

            @Override
            public String getSignature() {
                return jwt.getSignature();
            }

            @Override
            public String getAlgorithm() {
                return jwt.getAlgorithm();
            }

            @Override
            public String getType() {
                return jwt.getType();
            }

            @Override
            public String getContentType() {
                return jwt.getContentType();
            }

            @Override
            public String getKeyId() {
                return jwt.getKeyId();
            }

            @Override
            public Claim getHeaderClaim(String s) {
                com.auth0.jwt.interfaces.Claim claim = jwt.getHeaderClaim(s);
                return wrap(claim);
            }

            @Override
            public String getIssuer() {
                return jwt.getIssuer();
            }

            @Override
            public String getSubject() {
                return jwt.getSubject();
            }

            @Override
            public List<String> getAudience() {
                return jwt.getAudience();
            }

            @Override
            public Date getExpiresAt() {
                return jwt.getExpiresAt();
            }

            @Override
            public Date getNotBefore() {
                return jwt.getNotBefore();
            }

            @Override
            public Date getIssuedAt() {
                return jwt.getIssuedAt();
            }

            @Override
            public String getId() {
                return jwt.getId();
            }

            @Override
            public Claim getClaim(String s) {
                com.auth0.jwt.interfaces.Claim claim = jwt.getHeaderClaim(s);
                return wrap(claim);
            }

            @Override
            public Map<String, Claim> getClaims() {
                Map<String, com.auth0.jwt.interfaces.Claim> claims = jwt.getClaims();
                return claims.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> wrap(e.getValue())));
            }
        };
    }

    private Claim wrap(com.auth0.jwt.interfaces.Claim claim) {
        return new Claim() {
            @Override
            public boolean isNull() {
                return claim.isNull();
            }

            @Override
            public Boolean asBoolean() {
                return claim.asBoolean();
            }

            @Override
            public Integer asInt() {
                return claim.asInt();
            }

            @Override
            public Long asLong() {
                return claim.asLong();
            }

            @Override
            public Double asDouble() {
                return claim.asDouble();
            }

            @Override
            public String asString() {
                return claim.asString();
            }

            @Override
            public Date asDate() {
                return claim.asDate();
            }

            @Override
            public <T> T[] asArray(Class<T> aClass) throws JWTDecodeException {
                return claim.asArray(aClass);
            }

            @Override
            public <T> List<T> asList(Class<T> aClass) throws JWTDecodeException {
                return claim.asList(aClass);
            }

            @Override
            public Map<String, Object> asMap() throws JWTDecodeException {
                return claim.asMap();
            }

            @Override
            public <T> T as(Class<T> aClass) throws JWTDecodeException {
                return claim.as(aClass);
            }
        };
    }

    @Override
    protected String getKeyFactoryType() {
        return "EC";
    }
}
