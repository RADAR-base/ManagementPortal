package org.radarcns.auth.unit.filter;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.unit.util.TokenTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Created by dverbeec on 24/04/2017.
 */

public class TokenValidatorTest {

    private TokenValidator filter;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(TokenTestUtils.WIREMOCK_PORT);

    @BeforeClass
    public static void loadToken() throws Exception {
        TokenTestUtils.setUp();
    }

    @Before
    public void setUp() throws Exception {
        stubFor(get(urlEqualTo(TokenTestUtils.PUBLIC_KEY))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));
        filter = new TokenValidator();
    }

    @Test
    public void testValidateAccessToken() {
        filter.validateAccessToken(TokenTestUtils.VALID_TOKEN);
    }

    @Test(expected = TokenValidationException.class)
    public void testValidateAccessTokenForIncorrectAudience() {
        filter.validateAccessToken(TokenTestUtils.INCORRECT_AUDIENCE_TOKEN);
    }
}
