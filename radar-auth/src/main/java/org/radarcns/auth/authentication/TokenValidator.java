package org.radarcns.auth.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.token.JwtRadarToken;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.auth.token.validation.ECTokenValidationAlgorithm;
import org.radarcns.auth.token.validation.RSATokenValidationAlgorithm;
import org.radarcns.auth.token.validation.TokenValidationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates JWT token signed by the Management Portal. It is synchronized and may be used from
 * multiple threads. If the status of the public key should be checked immediately, call
 * {@link #refresh()} directly after creating this validator. It currently does not check this, so
 * that the validator can be used even if a remote ManagementPortal is not reachable during
 * construction.
 */
public class TokenValidator {

    protected static final Logger log = LoggerFactory.getLogger(TokenValidator.class);
    // additional required claims apart from the required JWT claims
    protected static final List<String> REQUIRED_CLAIMS = Arrays.asList(
            JwtRadarToken.GRANT_TYPE_CLAIM, JwtRadarToken.SCOPE_CLAIM);

    private final ServerConfig config;
    private List<JWTVerifier> verifiers = new LinkedList<>();
    private final List<TokenValidationAlgorithm> algorithmList = Arrays.asList(
            new ECTokenValidationAlgorithm(), new RSATokenValidationAlgorithm());

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
     * @deprecated Prefer {@link #TokenValidator(ServerConfig, Duration)} instead.
     */
    @Deprecated
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
        for (JWTVerifier verifier : getVerifiers()) {
            try {
                DecodedJWT jwt = verifier.verify(token);
                Set<String> claims = jwt.getClaims().keySet();
                Set<String> missing = REQUIRED_CLAIMS.stream()
                        .filter(c -> !claims.contains(c)).collect(Collectors.toSet());
                if (!missing.isEmpty()) {
                    throw new TokenValidationException("The following required claims were "
                            + "missing from the token: " + String.join(", ", missing));
                }
                return new JwtRadarToken(jwt);
            } catch (SignatureVerificationException sve) {
                log.warn("Client presented a token with an incorrect signature, fetching public "
                        + "keys again. Token: {}", token);
                refresh();
                return validateAccessToken(token);
            } catch (JWTVerificationException ex) {
                log.debug("Verifier {} with implementation {} did not accept token {}",
                        verifier.toString(), verifier.getClass().toString(), token);
            }
        }
        throw new TokenValidationException("No registered validator could authenticate this token");
    }

    private List<JWTVerifier> getVerifiers() {
        synchronized (this) {
            if (!verifiers.isEmpty()) {
                return verifiers;
            }
        }

        List<JWTVerifier> localVerifiers = loadVerifiers();

        synchronized (this) {
            verifiers = localVerifiers;
            return verifiers;
        }
    }

    /**
     * Refreshes the token verifier public key.
     * @throws TokenValidationException if the public key could not be refreshed.
     */
    public void refresh() throws TokenValidationException {
        List<JWTVerifier> localVerifiers = loadVerifiers();
        synchronized (this) {
            this.verifiers = localVerifiers;
        }
    }

    private List<JWTVerifier> loadVerifiers() throws TokenValidationException {
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

        List<Algorithm> algorithms = new LinkedList<>();
        if (config.getPublicKeyEndpoints() != null) {
            algorithms.addAll(config.getPublicKeyEndpoints().stream()
                    .map(this::algorithmFromServerPublicKey).collect(Collectors.toList()));
        }
        if (config.getPublicKeys() != null) {
            algorithms.addAll(config.getPublicKeys().stream()
                    .map(this::algorithmFromString).collect(Collectors.toList()));
        }

        // Create a verifier for each signature verification algorithm we created
        return algorithms.stream().map(alg -> JWT.require(alg)
                .withAudience(config.getResourceName())
                .build())
                .collect(Collectors.toList());
    }

    private Algorithm algorithmFromServerPublicKey(URI serverUri) throws TokenValidationException {
        log.info("Getting the JWT public key at " + serverUri);
        try {
            URLConnection connection =  serverUri.toURL().openConnection();
            connection.setRequestProperty("Accept", "application/json");
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode publicKeyInfo = mapper.readTree(inputStream);
                // We deny to trust the public key if the reported algorithm is unknown to us
                // https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                String alg = publicKeyInfo.get("alg").asText();
                String pk = publicKeyInfo.get("value").asText();
                return algorithmList.stream()
                        .filter(algorithm -> algorithm.getJwtAlgorithm().equals(alg))
                        .filter(algorithm -> pk.startsWith(algorithm.getKeyHeader()))
                        .findFirst()
                        .orElseThrow(() -> new TokenValidationException("The identity server "
                                + "reported an unsupported signing algorithm: " + alg))
                        .getAlgorithm(pk);
            }
        } catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private Algorithm algorithmFromString(String publicKey) {
        return algorithmList.stream()
                .filter(algorithm -> publicKey.startsWith(algorithm.getKeyHeader()))
                .findFirst()
                .orElseThrow(() -> new TokenValidationException("Unsupported public key: "
                        + publicKey))
                .getAlgorithm(publicKey);
    }
}
