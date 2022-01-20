package org.radarbase.auth.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.radarbase.auth.authorization.Permission;
import org.radarbase.auth.config.TokenValidatorConfig;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL;

/**
 * Created by dverbeec on 29/06/2017.
 */
public final class OAuthHelper {
    private static final Logger logger = LoggerFactory.getLogger(OAuthHelper.class);
    private static String validEcToken;
    private static String validRsaToken;

    public static final String TEST_KEYSTORE_PASSWORD = "radarbase";
    public static final String TEST_SIGNKEY_ALIAS = "radarbase-managementportal-ec";
    public static final String TEST_CHECKKEY_ALIAS = "radarbase-managementportal-rsa";
    public static final String[] SCOPES = Permission.scopes();
    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN"};
    public static final String[] ROLES = {"ROLE_SYS_ADMIN"};
    public static final String[] SOURCES = {};
    public static final String[] AUD = {RES_MANAGEMENT_PORTAL};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";
    private static List<JWTVerifier> verifiers;

    static {
        try {
            setUp();
        } catch (Exception e) {
            logger.error("Failed to set up OAuthHelper", e);
        }
    }

    private OAuthHelper() {
        // utility class
    }

    /**
     * Create a request post processor that adds a valid bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    public static RequestPostProcessor bearerToken() {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + validEcToken);
            return mockRequest;
        };
    }

    /**
     * Create a request post processor that adds a valid RSA bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    public static RequestPostProcessor rsaBearerToken() {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + validRsaToken);
            return mockRequest;
        };
    }

    /**
     * Set up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
     * @throws Exception If anything goes wrong during setup
     */
    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream keyStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("config/keystore.p12")) {
            if (keyStream == null) {
                throw new IllegalStateException("Cannot find keystore to set up OAuth");
            }

            ks.load(keyStream, TEST_KEYSTORE_PASSWORD.toCharArray());

            // get the EC keypair for signing
            ECPrivateKey privateKey = (ECPrivateKey) ks.getKey(TEST_SIGNKEY_ALIAS,
                    TEST_KEYSTORE_PASSWORD.toCharArray());
            Certificate cert = ks.getCertificate(TEST_SIGNKEY_ALIAS);
            ECPublicKey publicKey = (ECPublicKey) cert.getPublicKey();

            Algorithm ecdsa = Algorithm.ECDSA256(publicKey, privateKey);
            validEcToken = createValidToken(ecdsa);

            // also get an RSA keypair to test accepting multiple keys
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) ks.getKey(TEST_CHECKKEY_ALIAS,
                    TEST_KEYSTORE_PASSWORD.toCharArray());
            RSAPublicKey rsaPublicKey = (RSAPublicKey) ks.getCertificate(TEST_CHECKKEY_ALIAS)
                    .getPublicKey();
            Algorithm rsa = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
            validRsaToken = createValidToken(rsa);

            verifiers = Stream.of(ecdsa, rsa)
                    .map(alg -> JWT.require(alg).withIssuer(ISS).build())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Helper method to initialize an authentication filter for use in test classes.
     *
     * @return an initialized JwtAuthenticationFilter
     */
    public static JwtAuthenticationFilter createAuthenticationFilter() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findOneByLogin(anyString())).thenReturn(Optional.of(createAdminUser()));
        return new JwtAuthenticationFilter(createTokenValidator(), auth -> auth, userRepository);
    }

    /**
     * Helper method to initialize a token validator for use in test classes.
     *
     * @return configured TokenValidator
     */
    public static TokenValidator createTokenValidator() {
        // Use tokenValidator with known JWTVerifier which signs.
        return new TokenValidator.Builder()
                .verifiers(verifiers)
                .config(getDummyValidatorConfig())
                .fetchTimeout(Duration.ofHours(1))
                .build();
    }

    private static TokenValidatorConfig getDummyValidatorConfig() {
        return new TokenValidatorConfig() {
            @Override
            public List<URI> getPublicKeyEndpoints() {
                return Collections.emptyList();
            }

            @Override
            public String getResourceName() {
                return "ISS";
            }
        };
    }

    private static User createAdminUser() {
        User user = new User();
        user.setId(1L);
        user.setLogin("admin");
        user.setActivated(true);
        user.setRoles(Set.of(
            new Role(new Authority("ROLE_SYS_ADMIN"))
        ));
        return user;
    }

    private static String createValidToken(Algorithm algorithm) {
        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();
        return JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience("res_ManagementPortal")
                .withSubject(USER)
                .withArrayClaim("scope", SCOPES)
                .withArrayClaim("authorities", AUTHORITIES)
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", SOURCES)
                .withArrayClaim("aud", AUD)
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);
    }
}
