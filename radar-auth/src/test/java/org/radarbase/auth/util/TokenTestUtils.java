package org.radarbase.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.radarbase.auth.authorization.Permission;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Sets up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
 */
public final class TokenTestUtils {
    public static final String PUBLIC_KEY = "/oauth/token_key";
    public static final String PUBLIC_KEY_BODY;
    public static final String VALID_RSA_TOKEN;
    public static final String INCORRECT_AUDIENCE_TOKEN;
    public static final String EXPIRED_TOKEN;
    public static final String INCORRECT_ALGORITHM_TOKEN;
    public static final DecodedJWT SCOPE_TOKEN;
    public static final DecodedJWT PROJECT_ADMIN_TOKEN;
    public static final DecodedJWT SUPER_USER_TOKEN;
    public static final DecodedJWT MULTIPLE_ROLES_IN_PROJECT_TOKEN;

    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    public static final String[] ALL_SCOPES = allScopes();
    public static final String[] ROLES = {"PROJECT1:ROLE_PROJECT_ADMIN",
            "PROJECT2:ROLE_PARTICIPANT"};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";
    public static final String PUBLIC_KEY_STRING;

    public static final String APPLICATION_JSON = "application/json";
    public static final int WIREMOCK_PORT = 8089;

    private TokenTestUtils() {
        // utility class
    }

    static {
        RSAKeyProvider provider;
        try {
            provider = loadKeys();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to load keys for test", e);
        }
        RSAPublicKey publicKey = provider.getPublicKeyById("selfsigned");
        PUBLIC_KEY_STRING = new String(Base64.getEncoder().encode(
                provider.getPublicKeyById("selfsigned").getEncoded()));
        Algorithm algorithm = Algorithm.RSA256(publicKey, provider.getPrivateKey());

        PUBLIC_KEY_BODY = "{\n \"keys\" : [ {\n  \"alg\" : \"" + algorithm.getName()
                + "\",\n  \"alg\" : \"RSA\",\n"
                + "  \"value\" : \"-----BEGIN PUBLIC KEY-----\\n" + PUBLIC_KEY_STRING
                + "\\n-----END PUBLIC KEY-----\"\n} ]\n}";

        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();

        VALID_RSA_TOKEN = initValidToken(algorithm, exp, iat);
        SUPER_USER_TOKEN = JWT.decode(VALID_RSA_TOKEN);
        PROJECT_ADMIN_TOKEN = initProjectAdminToken(algorithm, exp, iat);
        MULTIPLE_ROLES_IN_PROJECT_TOKEN = initMultipleRolesToken(algorithm, exp, iat);
        INCORRECT_AUDIENCE_TOKEN = initIncorrectAudienceToken(algorithm, exp, iat);
        SCOPE_TOKEN = initTokenWithScopes(algorithm, exp, iat);
        INCORRECT_ALGORITHM_TOKEN = initIncorrectAlgorithmToken(exp, iat);

        Instant past = Instant.now().minusSeconds(1);
        Instant iatpast = Instant.now().minusSeconds(30 * 60 + 1);
        EXPIRED_TOKEN = initExpiredToken(algorithm, past, iatpast);
    }

    private static RSAKeyProvider loadKeys() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream keyStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("keystore.p12")) {
            ks.load(keyStream, "radarbase".toCharArray());
        }
        RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey("selfsigned",
                "radarbase".toCharArray());
        Certificate cert = ks.getCertificate("selfsigned");
        return new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                return (RSAPublicKey) cert.getPublicKey();
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                return privateKey;
            }

            @Override
            public String getPrivateKeyId() {
                return null;
            }
        };
    }

    private static String initExpiredToken(Algorithm algorithm, Instant past, Instant iatpast) {
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iatpast))
            .withExpiresAt(Date.from(past))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm);
    }

    private static String initIncorrectAlgorithmToken(Instant exp, Instant iat) {
        Algorithm psk = Algorithm.HMAC256("super-secret-stuff");
        // token signed with a pre-shared key
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", new String[] {})
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(psk);
    }

    private static String initIncorrectAudienceToken(Algorithm algorithm, Instant exp,
            Instant iat) {
        return JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience("SOME_AUDIENCE")
                .withSubject(USER)
                .withArrayClaim("scope", ALL_SCOPES)
                .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", new String[] {})
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);
    }

    private static DecodedJWT initMultipleRolesToken(Algorithm algorithm, Instant exp,
            Instant iat) {
        String multipleRolesInProjectToken = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(CLIENT)
                .withSubject(USER)
                .withArrayClaim("scope", ALL_SCOPES)
                .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
                .withArrayClaim("roles", new String[] {"PROJECT2:ROLE_PROJECT_ADMIN",
                        "PROJECT2:ROLE_PARTICIPANT"})
                .withArrayClaim("sources", new String[] {"source-1"})
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);

        return JWT.decode(multipleRolesInProjectToken);
    }

    private static DecodedJWT initProjectAdminToken(Algorithm algorithm, Instant exp, Instant iat) {
        String projectAdminToken = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(CLIENT)
                .withSubject(USER)
                .withArrayClaim("scope", ALL_SCOPES)
                .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN",
                        "ROLE_PARTICIPANT"})
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", new String[] {})
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);

        return JWT.decode(projectAdminToken);
    }

    private static String initValidToken(Algorithm algorithm, Instant exp, Instant iat) {
        return JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(CLIENT)
                .withSubject(USER)
                .withArrayClaim("scope", ALL_SCOPES)
                .withArrayClaim("authorities", AUTHORITIES)
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", SOURCES)
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);
    }

    private static DecodedJWT initTokenWithScopes(Algorithm algorithm, Instant exp, Instant iat) {
        String token = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(CLIENT)
                .withSubject("i'm a trusted oauth client")
                .withArrayClaim("scope", new String[] {"PROJECT.READ", "SUBJECT.CREATE",
                        "SUBJECT.READ", "MEASUREMENT.CREATE"})
                .withClaim("client_id", "i'm a trusted oauth client")
                .withClaim("jti", JTI)
                .withClaim("grant_type", "client_credentials")
                .sign(algorithm);
        return JWT.decode(token);
    }

    private static String[] allScopes() {
        return Permission.stream()
                .map(Permission::scopeName)
                .toArray(String[]::new);
    }
}
