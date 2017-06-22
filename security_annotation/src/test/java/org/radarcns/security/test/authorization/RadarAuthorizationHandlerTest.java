package org.radarcns.security.test.authorization;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.security.authorization.RadarAuthorizationHandler;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.exceptions.NotAuthorizedException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

/**
 * Created by dverbeec on 6/04/2017.
 */

public class RadarAuthorizationHandlerTest {

    private static final String OAUTH2_INTROSPECT = "/oauth2/check_token";
    private static final String PUBLIC_KEY = "/oauth2/token_key";
    private static String PUBLIC_KEY_BODY;
    private static String VALID_TOKEN;
    private static String EXPIRED_TOKEN;

    private static RadarAuthorizationHandler HANDLER;
    private static String SUCCESSFUL_RESPONSE;

    private static final String INVALID_RESPONSE = "{\"scope\":\"read_user\", "
                + "\"token_type\":\"Bearer\",\"exp\":1491492693,\"iat\":1491489093, "
                + "\"client_id\":\"35GaiYHy_7o5Vj3Qi3IaN1Iqnfwa\","
                + "\"username\":\"testuser1\"}";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    private static final int WIREMOCK_PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WIREMOCK_PORT);

    @BeforeClass
    public static void setUp() throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException, KeyStoreException, CertificateException, UnrecoverableKeyException {
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

    @Before
    public void initHandler() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        stubFor(get(urlEqualTo(PUBLIC_KEY))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(PUBLIC_KEY_BODY)));
        HANDLER = new RadarAuthorizationHandler(createMockServerConfig());
    }

    @Test
    public void testSuccessfulAuth() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(SUCCESSFUL_RESPONSE)));

        DecodedJWT jwt = HANDLER.validateAccessToken(VALID_TOKEN);
        assertEquals(2, jwt.getClaim("scope").asList(String.class).size());
        assertEquals("admin", jwt.getClaim("user_name").asString());
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidResponse() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withBody(INVALID_RESPONSE)));

        HANDLER.validateAccessToken(VALID_TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testServerError() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(500)));

        HANDLER.validateAccessToken(VALID_TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidToken() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withBody("{\"error\":\"invalid token\"}")));

        HANDLER.validateAccessToken(VALID_TOKEN);
    }

    @Test(expected = JWTVerificationException.class)
    public void testExpiredToken() throws IOException, NotAuthorizedException,
            JWTVerificationException {
        // no need for a check_token stub, token is validated before cache is checked, since the
        // token is expired, a JWTVerificationException should be thrown
        HANDLER.validateAccessToken(EXPIRED_TOKEN);
    }

    private static ServerConfig createMockServerConfig() {
        return new ServerConfig() {
            @Override
            public String getTokenValidationEndpoint() {
                return "http://localhost:" + WIREMOCK_PORT + OAUTH2_INTROSPECT;
            }

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
        String[] scopes = {"scope1", "scope2"};
        String[] authorities = {"ROLE_SYS_ADMIN", "ROLE_USER"};
        String[] roles = {":ROLE_SYS_ADMIN", ":ROLE_USER",
            "PROJECT1:ROLE_PROJECT_ADMIN", "PROJECT2:ROLE_PARTICIPANT"};
        String[] sources = {};
        String client = "oauth_client";
        String user = "admin";
        String iss = "RADAR";
        String jti = "some-jwt-id";

        VALID_TOKEN = JWT.create()
            .withIssuer(iss)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience(client)
            .withSubject(user)
            .withArrayClaim("scope", scopes)
            .withArrayClaim("authorities", authorities)
            .withArrayClaim("roles", roles)
            .withArrayClaim("sources", sources)
            .withClaim("client_id", client)
            .withClaim("user_name", user)
            .withClaim("jti", jti)
            .sign(algorithm);

        EXPIRED_TOKEN = JWT.create()
            .withIssuer(iss)
            .withIssuedAt(Date.from(iatpast))
            .withExpiresAt(Date.from(past))
            .withAudience(client)
            .withSubject(user)
            .withArrayClaim("scope", scopes)
            .withArrayClaim("authorities", authorities)
            .withArrayClaim("roles", roles)
            .withArrayClaim("sources", sources)
            .withClaim("client_id", client)
            .withClaim("user_name", user)
            .withClaim("jti", jti)
            .sign(algorithm);

        Map<String, Object> checkTokenResponseObject = new HashMap<>();
        checkTokenResponseObject.put("iss", iss);
        checkTokenResponseObject.put("iat", iat.getEpochSecond());
        checkTokenResponseObject.put("exp", exp.getEpochSecond());
        checkTokenResponseObject.put("aud", client);
        checkTokenResponseObject.put("sub", user);
        checkTokenResponseObject.put("scope", scopes);
        checkTokenResponseObject.put("authorities", authorities);
        checkTokenResponseObject.put("roles", roles);
        checkTokenResponseObject.put("sources", sources);
        checkTokenResponseObject.put("client_id", client);
        checkTokenResponseObject.put("user_name", user);
        checkTokenResponseObject.put("jti", jti);

        SUCCESSFUL_RESPONSE = JSONObject.toJSONString(checkTokenResponseObject);
    }
}
