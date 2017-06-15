package org.radarcns.security.test.authorization;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.security.authorization.RadarAuthorizationHandler;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Date;

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

    private Logger log = LoggerFactory.getLogger(RadarAuthorizationHandlerTest.class);

    private static final String OAUTH2_INTROSPECT = "/oauth2/check_token";
    private static final String PUBLIC_KEY = "/oauth2/token_key";
    private static String PUBLIC_KEY_BODY;
    private static String TOKEN;
    private static RadarAuthorizationHandler HANDLER;
    private static String SUCCESSFULL_RESPONSE;

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
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair kp = keyGen.generateKeyPair();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec publicKeySpec = kf.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec privateKeySpec = kf.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec);
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(privateKeySpec);

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
                        .withBody(SUCCESSFULL_RESPONSE)));

        DecodedJWT jwt = HANDLER.validateAccessToken(TOKEN);
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

        HANDLER.validateAccessToken(TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testServerError() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(500)));

        HANDLER.validateAccessToken(TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidToken() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withBody("{\"error\":\"invalid token\"}")));

        HANDLER.validateAccessToken(TOKEN);
    }

    private static ServerConfig createMockServerConfig() {
        return new ServerConfig() {
            @Override
            public URI tokenValidationEndpoint() {
                try {
                    return new URI("http://localhost:" + WIREMOCK_PORT + OAUTH2_INTROSPECT);
                }
                catch (Exception e) {
                    // should not happen
                    return null;
                }
            }

            @Override
            public URI publicKeyEndpoint() {
                try {
                    return new URI("http://localhost:" + WIREMOCK_PORT + PUBLIC_KEY);
                }
                catch (Exception e) {
                    // should not happen
                    return null;
                }
            }

            @Override
            public String username() {
                return "oauth_client";
            }

            @Override
            public String password() {
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

        Instant exp = Instant.now().plusSeconds(30*60);
        TOKEN = JWT.create()
            .withIssuer("RADAR")
            .withAudience("oauth_client")
            .withArrayClaim("scope", new String[]{"scope1", "scope2"})
            .withArrayClaim("authorities", new String[]{"ROLE_ADMIN"})
            .withClaim("client_id","oauth_client")
            .withClaim("user_name","admin")
            .withClaim("jti","some-jwt-id")
            .withExpiresAt(Date.from(exp))
            .sign(algorithm);

        SUCCESSFULL_RESPONSE = "{\n"
            + "  \"aud\" : [ \"oauth_client\" ],\n"
            + "  \"user_name\" : \"admin\",\n"
            + "  \"scope\" : [ \"scope1\", \"scope2\" ],\n"
            + "  \"exp\" : " + exp.getEpochSecond() + ",\n"
            + "  \"authorities\" : [ \"ROLE_ADMIN\" ],\n"
            + "  \"jti\" : \"some-jwt-id\",\n"
            + "  \"client_id\" : \"oauth_client\"\n"
            + "}";
    }
}
