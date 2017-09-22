package org.radarcns.auth.unit.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.binary.Base64;
import org.radarcns.auth.config.ServerConfig;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

/**
 * Created by dverbeec on 29/06/2017.
 */
public class TokenTestUtils {
    public static final String PUBLIC_KEY = "/oauth/token_key";
    public static String PUBLIC_KEY_BODY;
    public static String VALID_TOKEN;
    public static String INCORRECT_AUDIENCE_TOKEN;
    public static String EXPIRED_TOKEN;
    public static String INCORRECT_ALGORITHM_TOKEN;
    public static DecodedJWT PROJECT_ADMIN_TOKEN;
    public static DecodedJWT SUPER_USER_TOKEN;

    public static final String[] SCOPES = {"scope1", "scope2"};
    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    public static final String[] ROLES = {"PROJECT1:ROLE_PROJECT_ADMIN", "PROJECT2:ROLE_PARTICIPANT"};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";

    public static final String APPLICATION_JSON = "application/json";
    public static final int WIREMOCK_PORT = 8089;

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
        initVars(new String(new Base64().encode(publicKey.getEncoded())),
            Algorithm.RSA256(publicKey, privateKey));
    }

    private static void initVars(String publicKey, Algorithm algorithm) {
        PUBLIC_KEY_BODY = "{\n"
            + "  \"alg\" : \"SHA256withRSA\",\n"
            + "  \"value\" : \"-----BEGIN PUBLIC KEY-----\\n" + publicKey + "\\n-----END PUBLIC "
            + "KEY-----\"\n"
            + "}";

        Instant past = Instant.now().minusSeconds(1);
        Instant iatpast = Instant.now().minusSeconds(30*60+1);
        Instant exp = Instant.now().plusSeconds(30*60);
        Instant iat = Instant.now();

        VALID_TOKEN = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .sign(algorithm);

        SUPER_USER_TOKEN = JWT.decode(VALID_TOKEN);

        String projectAdminToken = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", new String[] {})
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .sign(algorithm);

        PROJECT_ADMIN_TOKEN = JWT.decode(projectAdminToken);

        INCORRECT_AUDIENCE_TOKEN = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience("SOME_AUDIENCE")
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", new String[] {})
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .sign(algorithm);

        Algorithm psk = null;
        try {
            psk = Algorithm.HMAC256("super-secret-stuff");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", new String[] {"ROLE_PROJECT_ADMIN"})
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", new String[] {})
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .sign(psk);

        EXPIRED_TOKEN = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iatpast))
            .withExpiresAt(Date.from(past))
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .sign(algorithm);
    }

    public static ServerConfig createMockServerConfig() {
        return new ServerConfig() {
            public URI getPublicKeyEndpoint() {
                try {
                    return new URI("http://localhost:" + WIREMOCK_PORT + PUBLIC_KEY);
                }
                catch (URISyntaxException ex) {
                    // should not happen
                    return null;
                }
            }

            @Override
            public String getResourceName() {
                return "unit_test";
            }
        };
    }
}
