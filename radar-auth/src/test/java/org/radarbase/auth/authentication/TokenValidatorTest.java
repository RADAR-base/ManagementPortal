package org.radarbase.auth.authentication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.File;
import java.net.URISyntaxException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.radarbase.auth.config.TokenVerifierPublicKeyConfig;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.util.TokenTestUtils;

/**
 * Created by dverbeec on 24/04/2017.
 */

public class TokenValidatorTest {

    private TokenValidator validator;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(TokenTestUtils.WIREMOCK_PORT);

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeClass
    public static void loadToken() throws Exception {
        TokenTestUtils.setUp();
    }

    /**
     * Set up a stub public key endpoint and initialize a TokenValidator object.
     *
     * @throws Exception if anything went wrong during setup
     */
    @Before
    public void setUp() throws Exception {
        stubFor(get(urlEqualTo(TokenTestUtils.PUBLIC_KEY)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));
        validator = new TokenValidator();
    }

    @Test
    public void testValidToken() {
        validator.validateAccessToken(TokenTestUtils.VALID_RSA_TOKEN);
    }

    @Test(expected = TokenValidationException.class)
    public void testIncorrectAudienceToken() {
        validator.validateAccessToken(TokenTestUtils.INCORRECT_AUDIENCE_TOKEN);
    }

    @Test(expected = TokenValidationException.class)
    public void testExpiredToken() {
        validator.validateAccessToken(TokenTestUtils.EXPIRED_TOKEN);
    }

    @Test(expected = TokenValidationException.class)
    public void testIncorrectAlgorithmToken() {
        validator.validateAccessToken(TokenTestUtils.INCORRECT_ALGORITHM_TOKEN);
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
