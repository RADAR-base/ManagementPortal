package org.radarcns.auth.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class TokenValidator {

    protected static final Logger log = LoggerFactory.getLogger(TokenValidator.class);

    private final ServerConfig config;
    private JWTVerifier verifier;

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable.
     * @throws IOException The configuration file is not accessible
     * @throws InvalidKeySpecException
     */
    public TokenValidator() throws TokenValidationException {
        this.config = YamlServerConfig.readFromFileOrClasspath();
        loadPublicKey();
    }

    public DecodedJWT validateAccessToken(String token) throws TokenValidationException {
        try {
            return verifier.verify(token);
        }
        catch (JWTVerificationException ex) {
            // perhaps the server's key changed, let's fetch it again and re-check
            loadPublicKey();
            try {
                return verifier.verify(token);
            }
            catch(JWTVerificationException ex2) {
                throw new TokenValidationException(ex2);
            }
        }
    }

    private void loadPublicKey() throws TokenValidationException {
        RSAPublicKey publicKey = publicKeyFromServer();
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        verifier = JWT.require(alg)
            .withAudience(config.getResourceName())
            .build();
    }

    private RSAPublicKey publicKeyFromServer() throws TokenValidationException {
        log.info("Getting the JWT public key at " + config.getPublicKeyEndpoint());

        try {
            URLConnection connection =  config.getPublicKeyEndpoint().toURL().openConnection();
            connection.setRequestProperty("Accept", "application/json");
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode publicKeyInfo = mapper.readTree(inputStream);

                // we expect RSA algorithm, and deny to trust the public key otherwise
                // see also https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                if (!publicKeyInfo.get("alg").asText().equals("SHA256withRSA")) {
                    throw new TokenValidationException("The identity server reported the following "
                        + "signing algorithm: " + publicKeyInfo.get("alg") + ". Expected SHA256withRSA.");
                }

                String keyString = publicKeyInfo.get("value").asText();
                return publicKeyFromString(keyString);
            }
        }
        catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private RSAPublicKey publicKeyFromString(String keyString) throws TokenValidationException {
        log.debug("Parsing public key: " + keyString);
        try(PemReader pemReader = new PemReader(new StringReader(keyString))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        }
        catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }
}
