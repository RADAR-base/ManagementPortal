package org.radarbase.auth.authentication;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarbase.auth.config.TokenValidatorConfig;
import org.radarbase.auth.config.TokenVerifierPublicKeyConfig;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.security.jwk.JavaWebKeySet;
import org.radarbase.auth.token.JwtRadarToken;
import org.radarbase.auth.token.RadarToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates JWT token signed by the Management Portal. It is synchronized and may be used from
 * multiple threads. If the status of the public key should be checked immediately, call
 * {@link #refresh()} directly after creating this validator. It currently does not check this, so
 * that the validator can be used even if a remote ManagementPortal is not reachable during
 * construction.
 */
public class TokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidator.class);
    private final TokenValidatorConfig config;
    private List<JWTVerifier> verifiers = new LinkedList<>();


    // If a client presents a token with an invalid signature, it might be the keypair was changed.
    // In that case we need to fetch it again, but we don't want a malicious client to be able to
    // make us DOS our own identity server. Fetching it at maximum once per minute mitigates this.
    private static final Duration FETCH_TIMEOUT_DEFAULT = Duration.ofMinutes(1);
    private final Duration fetchTimeout;
    private Instant lastFetch = Instant.MIN;

    private static final long DEFAULT_TIMEOUT = 30;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private final AlgorithmLoader algorithmLoader = new AlgorithmLoader();

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable. Will also fetch the public key from the
     * identity server for checking token signatures.
     */
    public TokenValidator() {
        this(TokenVerifierPublicKeyConfig.readFromFileOrClasspath(), FETCH_TIMEOUT_DEFAULT);
    }

    /**
     * Constructor where TokenValidatorConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     */
    public TokenValidator(TokenValidatorConfig config) {
        this(config, FETCH_TIMEOUT_DEFAULT);
    }

    /**
     * Constructor where TokenValidatorConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     * @param fetchTimeout timeout for retrying the public RSA key
     */
    private TokenValidator(TokenValidatorConfig config, Duration fetchTimeout) {
        this.fetchTimeout = fetchTimeout;
        this.config = config;
    }

    /**
     * Constructor where TokenValidatorConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     * @param fetchTimeout timeout for retrying the public RSA key in seconds
     * @deprecated Prefer {@link #TokenValidator(TokenValidatorConfig, Duration)} instead.
     */
    @Deprecated
    public TokenValidator(TokenValidatorConfig config, long fetchTimeout) {
        this(config, Duration.ofSeconds(fetchTimeout));
    }


    /**
     * Added for testing verifications with known verifiers.
     * @param verifiers knows verifiers that have signed the tokens for testing.
     * @param config instance of {@link TokenValidatorConfig} for testing.
     */
    @Deprecated
    protected TokenValidator(List<JWTVerifier> verifiers, TokenValidatorConfig config) {
        this.config = config;
        this.verifiers = verifiers;
        this.fetchTimeout = Duration.ofHours(1);
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
        return validateAccessToken(token, true);
    }

    private RadarToken validateAccessToken(String token, boolean tryRefresh) {
        List<JWTVerifier> localVerifiers = getVerifiers();
        for (JWTVerifier verifier : localVerifiers) {
            try {
                DecodedJWT jwt = verifier.verify(token);

                Map<String, Claim> claims = jwt.getClaims();

                // Do not print full token with signature to avoid exposing valid token in logs.
                LOGGER.debug("Verified JWT header {} and payload {}",
                        jwt.getHeader(), jwt.getPayload());

                // check for scope claim
                if (!claims.containsKey(JwtRadarToken.SCOPE_CLAIM)) {
                    throw new TokenValidationException("The required claim "
                            + JwtRadarToken.SCOPE_CLAIM + "is missing from the token");
                }
                return new JwtRadarToken(jwt);
            } catch (SignatureVerificationException sve) {
                LOGGER.debug("Client presented a token with an incorrect signature.");
                if (tryRefresh) {
                    LOGGER.info("Trying to fetch public keys again...");
                    try {
                        refresh();
                    } catch (TokenValidationException ex) {
                        // Log and Continue with validation
                        LOGGER.warn("Could not fetch public keys.", ex);
                    }
                    return validateAccessToken(token, false);
                }
            } catch (JWTVerificationException ex) {
                LOGGER.debug("Verifier {} with implementation {} did not accept token {}",
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
        if (!localVerifiers.isEmpty()) {
            synchronized (this) {
                this.verifiers = localVerifiers;
            }
        }
    }

    private List<JWTVerifier> loadVerifiers() throws TokenValidationException {
        synchronized (this) {
            // whether successful or not, do not request the key more than once per minute
            if (Instant.now().isBefore(lastFetch.plus(fetchTimeout))) {
                // it hasn't been long enough ago to fetch the key again, we deny access
                LOGGER.warn("Fetched public key less than {} ago, denied access.", fetchTimeout);
                throw new TokenValidationException("Not fetching public key more than once every "
                    + fetchTimeout);
            }
            lastFetch = Instant.now();
        }

        Stream<Algorithm> endpointKeys = streamEmptyIfNull(config.getPublicKeyEndpoints())
                .map(this::algorithmFromServerPublicKeyEndpoint)
                .filter(Objects::nonNull)
                .flatMap(List::stream);

        Stream<Algorithm> stringKeys = streamEmptyIfNull(config.getPublicKeys())
                .map(algorithmLoader::loadDeprecatedAlgorithmFromPublicKey);

        // Create a verifier for each signature verification algorithm we created
        return Stream.concat(endpointKeys, stringKeys)
                .map(alg -> AlgorithmLoader.buildVerifier(alg, config.getResourceName()))
                .collect(Collectors.toList());
    }

    private List<Algorithm> algorithmFromServerPublicKeyEndpoint(URI serverUri) throws
            TokenValidationException {
        LOGGER.info("Getting the JWT public key at " + serverUri);
        try {
            Request request = new Request.Builder()
                    .url(serverUri.toURL())
                    .header("Accept", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                JavaWebKeySet publicKeyInfo = mapper.readValue(response.body().string(),
                        JavaWebKeySet.class);
                response.close();
                LOGGER.debug("Processing {} public keys from public-key endpoint {}", publicKeyInfo
                        .getKeys().size(), serverUri.toURL());
                return algorithmLoader.loadAlgorithmsFromJavaWebKeys(publicKeyInfo);
            } else {
                // Log and Continue Pulling next Endpoints, if any
                LOGGER.warn("Invalid token signature. Could not load "+ "newer public keys");
                return null;
            }

        } catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private static <T> Stream<T> streamEmptyIfNull(Collection<T> collection) {
        return collection != null ? collection.stream() : Stream.empty();
    }
}
