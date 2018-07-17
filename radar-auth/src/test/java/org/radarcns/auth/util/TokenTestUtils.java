package org.radarcns.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.radarcns.auth.authorization.Permission;

/**
 * Created by dverbeec on 29/06/2017.
 */
public class TokenTestUtils {
    public static final String PUBLIC_KEY = "/oauth/token_key";
    public static String PUBLIC_KEY_BODY;
    public static String VALID_RSA_TOKEN;
    public static String INCORRECT_AUDIENCE_TOKEN;
    public static String EXPIRED_TOKEN;
    public static String INCORRECT_ALGORITHM_TOKEN;
    public static DecodedJWT SCOPE_TOKEN;
    public static DecodedJWT PROJECT_ADMIN_TOKEN;
    public static DecodedJWT SUPER_USER_TOKEN;
    public static DecodedJWT MULTIPLE_ROLES_IN_PROJECT_TOKEN;

    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    public static final String[] ALL_SCOPES = allScopes();
    public static final String[] ROLES = {"PROJECT1:ROLE_PROJECT_ADMIN",
            "PROJECT2:ROLE_PARTICIPANT"};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";
    public static String PUBLIC_KEY_STRING;

    public static final String APPLICATION_JSON = "application/json";
    public static final int WIREMOCK_PORT = 8089;

    /**
     * Set up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
     * @throws Exception If anything goes wrong during setup
     */
    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream keyStream = TokenTestUtils.class
                .getClassLoader().getResourceAsStream("keystore.jks");
        ks.load(keyStream, "radarbase".toCharArray());
        RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey("selfsigned",
                "radarbase".toCharArray());
        Certificate cert = ks.getCertificate("selfsigned");
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        keyStream.close();
        PUBLIC_KEY_STRING = new String(new Base64().encode(publicKey.getEncoded()));
        initVars(PUBLIC_KEY_STRING, Algorithm.RSA256(publicKey, privateKey));
    }

    private static void initVars(String publicKey, Algorithm algorithm) {
        PUBLIC_KEY_BODY = "{\n"
            + "  \"alg\" : \"SHA256withRSA\",\n"
            + "  \"value\" : \"-----BEGIN PUBLIC KEY-----\\n" + publicKey + "\\n-----END PUBLIC "
            + "KEY-----\"\n"
            + "}";

        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();

        initValidToken(algorithm, exp, iat);
        initProjectAdminToken(algorithm, exp, iat);
        initMultipleRolesToken(algorithm, exp, iat);
        initIncorrectAudienceToken(algorithm, exp, iat);
        initTokenWithScopes(algorithm, exp, iat);
        initIncorrectAlgorithmToken(exp, iat);

        Instant past = Instant.now().minusSeconds(1);
        Instant iatpast = Instant.now().minusSeconds(30 * 60 + 1);
        initExpiredToken(algorithm, past, iatpast);
    }

    private static void initExpiredToken(Algorithm algorithm, Instant past, Instant iatpast) {
        EXPIRED_TOKEN = JWT.create()
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

    private static void initIncorrectAlgorithmToken(Instant exp, Instant iat) {
        Algorithm psk = null;
        try {
            psk = Algorithm.HMAC256("super-secret-stuff");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        // token signed with a pre-shared key
        INCORRECT_ALGORITHM_TOKEN = JWT.create()
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

    private static void initIncorrectAudienceToken(Algorithm algorithm, Instant exp, Instant iat) {
        INCORRECT_AUDIENCE_TOKEN = JWT.create()
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

    private static void initMultipleRolesToken(Algorithm algorithm, Instant exp, Instant iat) {
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
                .withArrayClaim("sources", new String[] {})
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm);

        MULTIPLE_ROLES_IN_PROJECT_TOKEN = JWT.decode(multipleRolesInProjectToken);
    }

    private static void initProjectAdminToken(Algorithm algorithm, Instant exp, Instant iat) {
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

        PROJECT_ADMIN_TOKEN = JWT.decode(projectAdminToken);
    }

    private static void initValidToken(Algorithm algorithm, Instant exp, Instant iat) {
        VALID_RSA_TOKEN = JWT.create()
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
        SUPER_USER_TOKEN = JWT.decode(VALID_RSA_TOKEN);
    }

    private static void initTokenWithScopes(Algorithm algorithm, Instant exp, Instant iat) {
        String token = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(CLIENT)
                .withSubject("i'm a trusted oauth client")
                .withArrayClaim("scope", new String[] {"PROJECT.READ", "SUBJECT.CREATE",
                        "SUBJECT.READ"})
                .withClaim("client_id", "i'm a trusted oauth client")
                .withClaim("jti", JTI)
                .withClaim("grant_type", "client_credentials")
                .sign(algorithm);
        SCOPE_TOKEN = JWT.decode(token);
    }

    private static String[] allScopes() {
        return Permission.allPermissions().stream()
                .map(Permission::scopeName)
                .collect(Collectors.toList())
                .toArray(new String[Permission.allPermissions().size()]);
    }
}
