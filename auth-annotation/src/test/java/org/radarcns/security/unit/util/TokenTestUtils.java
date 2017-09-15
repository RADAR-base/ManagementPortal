package org.radarcns.security.unit.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.unit.authorization.RadarAuthorizationHandlerTest;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dverbeec on 29/06/2017.
 */
public class TokenTestUtils {
    public static final String OAUTH2_INTROSPECT = "/oauth/check_token";
    public static final String PUBLIC_KEY = "/oauth/token_key";
    public static String PUBLIC_KEY_BODY;
    public static String VALID_TOKEN;
    public static String EXPIRED_TOKEN;
    public static final String[] SCOPES = {"scope1", "scope2"};
    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    public static final String[] ROLES = {":ROLE_SYS_ADMIN", ":ROLE_USER",
        "PROJECT1:ROLE_PROJECT_ADMIN", "PROJECT2:ROLE_PARTICIPANT"};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "oauth_client";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";

    public static final String INVALID_RESPONSE = "{\"scope\":\"read_user\", "
        + "\"token_type\":\"Bearer\",\"exp\":1491492693,\"iat\":1491489093, "
        + "\"client_id\":\"35GaiYHy_7o5Vj3Qi3IaN1Iqnfwa\","
        + "\"username\":\"testuser1\"}";
    public static final String APPLICATION_JSON = "application/json";
    public static final int WIREMOCK_PORT = 8089;

    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream keyStream = RadarAuthorizationHandlerTest.class.getClassLoader()
            .getResourceAsStream("keystore.jks");
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
            @Override
            public String getPublicKeyEndpoint() {
                return "http://localhost:" + WIREMOCK_PORT + PUBLIC_KEY;
            }

            @Override
            public String getUsername() {
                return "oauth_client";
            }

            @Override
            public String getPassword() {
                return "oauth_client_secret";
            }
        };
    }
}
