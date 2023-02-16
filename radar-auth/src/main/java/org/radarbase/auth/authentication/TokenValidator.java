package org.radarbase.auth.authentication;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import org.radarbase.auth.token.validation.TokenValidationAlgorithm;
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
    private static final Duration FETCH_TIMEOUT_DEFAULT = Duration.ofMinutes(1);

    private final TokenValidatorConfig config;

    // If a client presents a token with an invalid signature, it might be the keypair was changed.
    // In that case we need to fetch it again, but we don't want a malicious client to be able to
    // make us DOS our own identity server. Fetching it at maximum once per minute mitigates this.
    private final Duration fetchTimeout;

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final AlgorithmLoader algorithmLoader;

    private List<JWTVerifier> verifiers;
    private Instant lastFetch = Instant.MIN;

    private TokenValidator(Builder builder) {
        this.mapper = builder.mapper;
        this.fetchTimeout = builder.fetchTimeout;
        this.client = builder.httpClient;
        this.config = builder.config;
        this.algorithmLoader = new AlgorithmLoader(builder.algorithms);
        this.verifiers = builder.verifiers;
    }

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable. Will also fetch the public key from the
     * identity server for checking token signatures.
     */
    public TokenValidator() {
        this(new Builder().verify());
    }

    /**
     * Constructor where TokenValidatorConfig can be passed instead of it being loaded from file.
     *
     * @param config The identity server configuration
     */
    public TokenValidator(TokenValidatorConfig config) {
        this(new Builder().config(config).verify());
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
        boolean signatureFailed = false;
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
                signatureFailed = true;
            } catch (JWTVerificationException ex) {
                LOGGER.debug("Verifier {} with implementation {} did not accept token",
                        verifier, verifier.getClass());
            }
        }
        if (signatureFailed && tryRefresh) {
            LOGGER.info("Trying to fetch public keys again...");
            try {
                refresh();
            } catch (TokenValidationException ex) {
                // Log and Continue with validation
                LOGGER.warn("Could not fetch public keys.", ex);
            }
            return validateAccessToken(token, false);
        } else {
            throw new TokenValidationException(
                    "No registered validator could authenticate this token");
        }
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

        return streamEmptyIfNull(config.getPublicKeyEndpoints())
                .map(this::algorithmFromServerPublicKeyEndpoint)
                .flatMap(List::stream)
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
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JavaWebKeySet publicKeyInfo = mapper.readValue(response.body().string(),
                            JavaWebKeySet.class);
                    LOGGER.debug("Processing {} public keys from public-key endpoint {}",
                            publicKeyInfo.getKeys().size(), serverUri.toURL());
                    return algorithmLoader.loadAlgorithmsFromJavaWebKeys(publicKeyInfo);
                } else {
                    throw new TokenValidationException("Invalid token signature. Could not load "
                            + "newer public keys");
                }
            }
        } catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private static <T> Stream<T> streamEmptyIfNull(Collection<T> collection) {
        return collection != null ? collection.stream() : Stream.empty();
    }

    /** Builder for the TokenValidator. Prefer this {@link #build()} method over the constructor
     * invocations. */
    public static class Builder {
        private static final int DEFAULT_HTTP_TIMEOUT = 30;

        public TokenValidatorConfig config;
        private OkHttpClient httpClient;
        private ObjectMapper mapper;
        private Duration fetchTimeout;
        private List<JWTVerifier> verifiers;
        private List<TokenValidationAlgorithm> algorithms;

        public Builder httpClient(OkHttpClient client) {
            this.httpClient = client;
            return this;
        }

        public Builder objectMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder fetchTimeout(Duration timeout) {
            this.fetchTimeout = timeout;
            return this;
        }

        public Builder config(TokenValidatorConfig config) {
            this.config = config;
            return this;
        }

        public Builder verifiers(List<JWTVerifier> verifiers) {
            this.verifiers = verifiers;
            return this;
        }

        public Builder validators(List<TokenValidationAlgorithm> validators) {
            this.algorithms = validators;
            return this;
        }

        private Builder verify() {
            if (httpClient == null) {
                httpClient = new OkHttpClient.Builder()
                        .connectTimeout(DEFAULT_HTTP_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(DEFAULT_HTTP_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(DEFAULT_HTTP_TIMEOUT, TimeUnit.SECONDS)
                        .build();
            } else {
                httpClient = httpClient.newBuilder().build();
            }
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            if (fetchTimeout == null) {
                fetchTimeout = FETCH_TIMEOUT_DEFAULT;
            }
            if (config == null) {
                config = TokenVerifierPublicKeyConfig.readFromFileOrClasspath();
            }
            if (algorithms == null) {
                algorithms = AlgorithmLoader.defaultAlgorithms();
            }
            if (verifiers == null) {
                verifiers = List.of();
            }
            return this;
        }

        /**
         * Build a new validator.
         * @return built validator.
         */
        public TokenValidator build() {
            verify();
            return new TokenValidator(this);
        }
    }
}
