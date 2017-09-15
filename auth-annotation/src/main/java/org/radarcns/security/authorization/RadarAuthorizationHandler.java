package org.radarcns.security.authorization;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.exception.TokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthorizationHandler implementation for use with the ManagementPortal.
 */
public class RadarAuthorizationHandler implements AuthorizationHandler {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorizationHandler.class);
    private final ServerConfig config;
    JWTVerifier verifier;

    public RadarAuthorizationHandler(ServerConfig config) throws TokenValidationException {
        this.config = config;
        loadPublicKey();
    }

    @Override
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

    @Override
    public ServerConfig getIdentityServerConfig() {
        return config;
    }

    private void loadPublicKey() throws TokenValidationException {
        RSAPublicKey publicKey = publicKeyFromServer();
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        verifier = JWT.require(alg).build();
    }

    private RSAPublicKey publicKeyFromServer() throws TokenValidationException {
        log.debug("Getting the public key at " + config.getPublicKeyEndpoint());

        try {
            URLConnection connection = new URL(config.getPublicKeyEndpoint()).openConnection();
            connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode publicKeyInfo = mapper.readTree(inputStream);

                // we expect RSA algorithm, and deny to trust the public key otherwise
                // see also https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                if (!publicKeyInfo.get("alg").asText().equals("SHA256withRSA")) {
                    throw new NotAuthorizedException("The identity server reported the following "
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
