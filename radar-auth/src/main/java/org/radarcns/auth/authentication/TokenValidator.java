package org.radarcns.auth.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.token.JwtRadarToken;
import org.radarcns.auth.token.RadarToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates JWT token signed by the Management Portal. It is synchronized and may be used from
 * multiple threads.
 */
public class TokenValidator {

    protected static final Logger log = LoggerFactory.getLogger(TokenValidator.class);
    // additional required claims apart from the required JWT claims
    protected static final List<String> REQUIRED_CLAIMS = Arrays.asList(
            JwtRadarToken.GRANT_TYPE_CLAIM, JwtRadarToken.SCOPE_CLAIM);

    private final ServerConfig config;
    private JWTVerifier verifier;

    // If a client presents a token with an invalid signature, it might be the keypair was changed.
    // In that case we need to fetch it again, but we don't want a malicious client to be able to
    // make us DOS our own identity server. Fetching it at maximum once per minute mitigates this.
    private static final Duration FETCH_TIMEOUT_DEFAULT = Duration.ofMinutes(1);
    private final Duration fetchTimeout;
    private Instant lastFetch = Instant.MIN;

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable. Will also fetch the public key from the
     * identity server for checking token signatures.
     */
    public TokenValidator() {
        this(YamlServerConfig.readFromFileOrClasspath(), FETCH_TIMEOUT_DEFAULT);
    }

    /**
     * Constructor where ServerConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     */
    public TokenValidator(ServerConfig config) {
        this(config, FETCH_TIMEOUT_DEFAULT);
    }

    /**
     * Constructor where ServerConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     * @param fetchTimeout timeout for retrying the public RSA key
     */
    public TokenValidator(ServerConfig config, Duration fetchTimeout) {
        this.fetchTimeout = fetchTimeout;
        this.config = config;
    }

    /**
     * Constructor where ServerConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     * @param fetchTimeout timeout for retrying the public RSA key in seconds
     */
    public TokenValidator(ServerConfig config, long fetchTimeout) {
        this(config, Duration.ofSeconds(fetchTimeout));
    }

    /**
     * Validates an access token and returns the decoded JWT as a {@link DecodedJWT} object.
     * <p>
     * If we have not yet fetched the JWT public key, this method will fetch it. If a signature can
     * not be verified, this method will fetch the JWT public key again, as it might have been
     * changed, and re-check the token. However this fetching of the public key will only be
     * performed at most once every <code>fetchTimeout</code> seconds, to prevent (malicious)
     * clients from making us call the token endpoint too frequently.
     * </p>
     *
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    public RadarToken validateAccessToken(String token) throws TokenValidationException {
        try {
            DecodedJWT jwt = getVerifier().verify(token);
            Set<String> claims = jwt.getClaims().keySet();
            Set<String> missing = REQUIRED_CLAIMS.stream()
                    .filter(c -> !claims.contains(c))
                    .collect(Collectors.toSet());
            if (!missing.isEmpty()) {
                throw new TokenValidationException("The following required claims were missing "
                        + "from the token: " + String.join(", ", missing));
            }
            return new JwtRadarToken(jwt);
        } catch (SignatureVerificationException sve) {
            log.warn("Client presented a token with an incorrect signature, fetching public key"
                    + " again. Token: {}", token);
            updateVerifier();
            return validateAccessToken(token);
        } catch (JWTVerificationException ex) {
            throw new TokenValidationException(ex);
        }
    }

    private JWTVerifier getVerifier() {
        synchronized (this) {
            if (verifier != null) {
                return verifier;
            }
        }

        JWTVerifier localVerifier = loadVerifier();

        synchronized (this) {
            verifier = localVerifier;
            return verifier;
        }
    }

    private void updateVerifier() {
        JWTVerifier localVerifier = loadVerifier();
        synchronized (this) {
            this.verifier = localVerifier;
        }
    }

    private JWTVerifier loadVerifier() throws TokenValidationException {
        synchronized (this) {
            // whether successful or not, do not request the key more than once per minute
            if (Instant.now().isBefore(lastFetch.plus(fetchTimeout))) {
                // it hasn't been long enough ago to fetch the key again, we deny access
                log.warn("Fetched public key less than {} ago, denied access.", fetchTimeout);
                throw new TokenValidationException("Not fetching public key more than once every "
                    + fetchTimeout);
            }
            lastFetch = Instant.now();
        }

        RSAPublicKey publicKey;
        if (config.getPublicKey() == null) {
            publicKey = publicKeyFromServer();
        } else {
            publicKey = config.getPublicKey();
        }
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        // we successfully fetched the public key, reset the timer
        return JWT.require(alg)
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
