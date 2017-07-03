package org.radarcns.security.unit.authorization;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.security.authorization.AuthorizationHandler;
import org.radarcns.security.authorization.RadarAuthorizationHandler;
import org.radarcns.security.unit.util.TokenTestUtils;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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

    private AuthorizationHandler HANDLER;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(TokenTestUtils.WIREMOCK_PORT);

    @BeforeClass
    public static void setUp() throws Exception {
        TokenTestUtils.setUp();
    }

    @Before
    public void initHandler() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            NotAuthorizedException {
        stubFor(get(urlEqualTo(TokenTestUtils.PUBLIC_KEY))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));
        HANDLER = new RadarAuthorizationHandler(TokenTestUtils.createMockServerConfig());
    }

    @Test
    public void testSuccessfulAuth() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(TokenTestUtils.OAUTH2_INTROSPECT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, TokenTestUtils.APPLICATION_JSON)
                        .withBody(TokenTestUtils.SUCCESSFUL_RESPONSE)));

        DecodedJWT jwt = HANDLER.validateAccessToken(TokenTestUtils.VALID_TOKEN);
        assertEquals(2, jwt.getClaim("scope").asList(String.class).size());
        assertEquals("admin", jwt.getClaim("user_name").asString());
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidResponse() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(TokenTestUtils.OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, TokenTestUtils.APPLICATION_JSON)
                                .withBody(TokenTestUtils.INVALID_RESPONSE)));

        HANDLER.validateAccessToken(TokenTestUtils.VALID_TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testServerError() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(TokenTestUtils.OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(500)));

        HANDLER.validateAccessToken(TokenTestUtils.VALID_TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidToken() throws IOException, NotAuthorizedException {
        stubFor(post(urlEqualTo(TokenTestUtils.OAUTH2_INTROSPECT))
                    .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, TokenTestUtils.APPLICATION_JSON)
                                .withBody("{\"error\":\"invalid token\"}")));

        HANDLER.validateAccessToken(TokenTestUtils.VALID_TOKEN);
    }

    @Test(expected = JWTVerificationException.class)
    public void testExpiredToken() throws IOException, NotAuthorizedException,
            JWTVerificationException {
        // no need for a check_token stub, token is validated before cache is checked, since the
        // token is expired, a JWTVerificationException should be thrown
        HANDLER.validateAccessToken(TokenTestUtils.EXPIRED_TOKEN);
    }
}
