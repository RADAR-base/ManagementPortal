package org.radarcns.auth.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;

public class TokenValidator {

    protected static final Logger log = LoggerFactory.getLogger(TokenValidator.class);

    private final ServerConfig config;
    private JWTVerifier verifier;

    // If a client presents a token with an invalid signature, it might be the keypair was changed.
    // In that case we need to fetch it again, but we don't want a malicious client to be able to
    // make us DOS our own identity server. Fetching it at maximum once per minute mitigates this.
    private Instant lastFetch;
    private static final long fetchPeriod = 60L;

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable. Will also fetch the public key from the
     * identity server for checkign token signatures.
     * @throws TokenValidationException If the config file can not be loaded, is incorrect, or the
     *     identity server's public key can not be fetched.
     */
    public TokenValidator() throws TokenValidationException {
        this.config = YamlServerConfig.readFromFileOrClasspath();

        try {
            // Catch this exception here, as the identity server might not be online when this class
            // is instantiated. We want this class to always be able to be instantiated, except for
            // config file errors.
            loadPublicKey();
        } catch (TokenValidationException ex) {
            log.error("Could not get server's public key.", ex);
        }
    }

    /**
     * Constructor where ServerConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     */
    public TokenValidator(ServerConfig config) {
        this.config = config;
        try {
            // Catch this exception here, as the identity server might not be online when this class
            // is instantiated. We want this class to always be able to be instantiated, except for
            // config file errors.
            loadPublicKey();
        } catch (TokenValidationException ex) {
            log.error("Could not get server's public key.", ex);
        }
    }

    /**
     * Validates an access token and returns the decoded JWT as a {@link DecodedJWT} object.
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    public DecodedJWT validateAccessToken(String token) throws TokenValidationException {
        if (verifier == null) {
            loadPublicKey();
        }
        try {
            return verifier.verify(token);
        } catch (SignatureVerificationException sve) {
            if (Instant.now().isAfter(lastFetch.plusSeconds(fetchPeriod))) {
                // perhaps the server's key changed, let's fetch it again and re-check
                log.warn("Client presented a token with an incorrect signature, fetching public key"
                        + " again. Token: {}", token);
                loadPublicKey();
                return validateAccessToken(token);
            } else {
                // it hasn't been long enough ago to fetch the key again, we deny access
                log.warn("Client presented a token with an incorrect signature, fetched public key "
                        + "less than {} seconds ago, denied access. Token: {}", fetchPeriod, token);
                throw new TokenValidationException(sve);
            }
        } catch (JWTVerificationException ex) {
            throw new TokenValidationException(ex);
        }
    }

    private void loadPublicKey() throws TokenValidationException {
        RSAPublicKey publicKey;
        if (config.getPublicKey() == null) {
            publicKey = publicKeyFromServer();
        } else {
            publicKey = config.getPublicKey();
        }
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        verifier = JWT.require(alg)
            .withAudience(config.getResourceName())
            .build();
        // we successfully fetched the public key, reset the timer
        lastFetch = Instant.now();
    }



    private RSAPublicKey publicKeyFromServer() throws TokenValidationException {
        log.info("Getting the JWT public key at " + config.getPublicKeyEndpoint());

        try {
            URLConnection connection =  config.getPublicKeyEndpoint().toURL().openConnection();
            connection.setRequestProperty("Accept", "application/json");
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode publicKeyInfo = mapper.readTree(inputStream);

                // We expect RSA algorithm, and deny to trust the public key otherwise, see also
                // https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                if (!publicKeyInfo.get("alg").asText().equals("SHA256withRSA")) {
                    throw new TokenValidationException("The identity server reported the following "
                        + "signing algorithm: " + publicKeyInfo.get("alg")
                        + ". Expected SHA256withRSA.");
                }

                String keyString = publicKeyInfo.get("value").asText();
                return publicKeyFromString(keyString);
            }
        } catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private RSAPublicKey publicKeyFromString(String keyString) throws TokenValidationException {
        log.debug("Parsing public key: " + keyString);
        try (PemReader pemReader = new PemReader(new StringReader(keyString))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }
}
