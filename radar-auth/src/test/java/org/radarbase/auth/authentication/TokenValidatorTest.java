package org.radarbase.auth.authentication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URISyntaxException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.radarbase.auth.config.TokenVerifierPublicKeyConfig;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.util.TokenTestUtils;

/**
 * Created by dverbeec on 24/04/2017.
 */

public class TokenValidatorTest {

    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private static WireMockServer wireMockServer;
    private TokenValidator validator;

    @BeforeAll
    public static void loadToken() throws Exception {
        wireMockServer = new WireMockServer(new WireMockConfiguration()
                .port(TokenTestUtils.WIREMOCK_PORT));
        wireMockServer.start();
        TokenTestUtils.setUp();
    }

    /**
     * Set up a stub public key endpoint and initialize a TokenValidator object.
     *
     * @throws Exception if anything went wrong during setup
     */
    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo(TokenTestUtils.PUBLIC_KEY)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));
        validator = new TokenValidator();
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
    public void testValidToken() {
        validator.validateAccessToken(TokenTestUtils.VALID_RSA_TOKEN);
    }

    @Test
    public void testIncorrectAudienceToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.validateAccessToken(TokenTestUtils.INCORRECT_AUDIENCE_TOKEN));
    }

    @Test
    public void testExpiredToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.validateAccessToken(TokenTestUtils.EXPIRED_TOKEN));
    }

    @Test
    public void testIncorrectAlgorithmToken() {
        assertThrows(TokenValidationException.class,
                () -> validator.validateAccessToken(TokenTestUtils.INCORRECT_ALGORITHM_TOKEN));
    }

    @Test
    public void testPublicKeyFromConfigFile() throws URISyntaxException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File configFile = new File(loader.getResource("radar-is-2.yml").toURI());
        environmentVariables
                .set(TokenVerifierPublicKeyConfig.LOCATION_ENV, configFile.getAbsolutePath());
        // reinitialize TokenValidator to pick up new config
        validator = new TokenValidator();
        validator.validateAccessToken(TokenTestUtils.VALID_RSA_TOKEN);
    }
}
