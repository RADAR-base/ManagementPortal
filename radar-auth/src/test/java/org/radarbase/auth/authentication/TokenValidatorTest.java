package org.radarbase.auth.authentication;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.jwks.JwkAlgorithmParser;
import org.radarbase.auth.jwks.JwksTokenVerifierLoader;
import org.radarbase.auth.jwks.RSAPEMCertificateParser;
import org.radarbase.auth.util.TokenTestUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.radarbase.auth.util.TokenTestUtils.WIREMOCK_PORT;

/**
 * Created by dverbeec on 24/04/2017.
 */

class TokenValidatorTest {

    private static WireMockServer wireMockServer;
    private TokenValidator validator;

    @BeforeAll
    public static void loadToken() throws Exception {
        wireMockServer = new WireMockServer(new WireMockConfiguration()
                .port(WIREMOCK_PORT));
        wireMockServer.start();
    }

    /**
     * Set up a stub public key endpoint and initialize a TokenValidator object.
     *
     */
    @BeforeEach
    public void setUp() {
        wireMockServer.stubFor(get(urlEqualTo(TokenTestUtils.PUBLIC_KEY)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));

        var algorithmParser = new JwkAlgorithmParser(List.of(new RSAPEMCertificateParser()));
        var verifierLoader = new JwksTokenVerifierLoader(
                "http://localhost:" + WIREMOCK_PORT + TokenTestUtils.PUBLIC_KEY,
                "unit_test",
                algorithmParser
        );
        validator = new TokenValidator(List.of(verifierLoader));
    }

    @AfterEach
    public void reset() {
        wireMockServer.resetAll();
    }

    @AfterAll
    public static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testValidToken() {
        validator.authenticateBlocking(TokenTestUtils.VALID_RSA_TOKEN);
    }

    @Test
    void testIncorrectAudienceToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.authenticateBlocking(TokenTestUtils.INCORRECT_AUDIENCE_TOKEN));
    }

    @Test
    void testExpiredToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.authenticateBlocking(TokenTestUtils.EXPIRED_TOKEN));
    }

    @Test
    void testIncorrectAlgorithmToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.authenticateBlocking(TokenTestUtils.INCORRECT_ALGORITHM_TOKEN));
    }
}
