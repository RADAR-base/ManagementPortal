package org.radarcns.oauth.unit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.oauth.OAuth2AccessToken;
import org.radarcns.oauth.OAuth2Client;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dverbeec on 31/08/2017.
 */
public class OAuth2ClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);


    @Before
    public void init() {
        tokenIssueDate = Instant.now().getEpochSecond();
    }


    @Test
    public void testValidTokenResponse() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(successfulResponse())));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertTrue(token.isValid());
        assertFalse(token.isExpired());
        assertEquals(accessToken, token.getAccessToken());
        assertEquals("bearer", token.getTokenType());
        assertEquals(1799L, token.getExpiresIn());
        assertEquals(tokenIssueDate, token.getIssueDate());
        assertEquals("radar_restapi", token.getSubject());
        assertEquals("read", token.getScope());
        assertEquals(accessTokenId, token.getJsonWebTokenId());
        assertEquals("ManagementPortal", token.getIssuer());
    }

    @Test
    public void testInvalidScope() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(invalidScopeResponse)));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("write");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("invalid_scope", token.getError());
        assertEquals("Invalid scope: write", token.getErrorDescription());
    }

    @Test
    public void testInvalidCredentials() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(invalidCredentialsResponse)));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("Unauthorized", token.getError());
        assertEquals("Bad credentials", token.getErrorDescription());
    }

    @Test
    public void testInvalidGrantType() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(invalidGrantTypeResponse)));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("invalid_client", token.getError());
        assertEquals("Unauthorized grant type: client_credentials", token.getErrorDescription());
    }

    @Test
    public void testInvalidMapping() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(invalidTypesResponse())));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("json_mapping_error", token.getError());
    }

    @Test
    public void testUnreachableServer() {
        // no http stub here so the location will be unreachable
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            // different port in case wiremock is not cleaned up yet
            .managementPortalUrl("http://localhost:9000")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("io_error", token.getError());
    }



    @Test
    public void testParseError() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/html")
                .withBody("<html>Oops, no JSON here</html>")));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("json_parse_error", token.getError());
    }

    @Test
    public void testNotFound() {
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withBody(notFoundResponse)));
        OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read");
        OAuth2AccessToken token = client.getAccessToken();
        assertFalse(token.isValid());
        assertTrue(token.isExpired());
        assertEquals("Not Found", token.getError());
    }

    private String successfulResponse() {
        return "{\n"
            + "  \"access_token\" : \"" + accessToken + "\",\n"
            + "  \"token_type\" : \"bearer\",\n"
            + "  \"expires_in\" : 1799,\n"
            + "  \"scope\" : \"read\",\n"
            + "  \"sub\" : \"radar_restapi\",\n"
            + "  \"sources\" : [ ],\n"
            + "  \"iss\" : \"ManagementPortal\",\n"
            + "  \"iat\" : " + tokenIssueDate + ",\n"
            + "  \"jti\" : \"" + accessTokenId + "\"\n"
            + "}";
    }

    private String invalidTypesResponse() {
        return "{\n"
            + "  \"access_token\" : \"" + accessToken + "\",\n"
            + "  \"token_type\" : \"bearer\",\n"
            + "  \"expires_in\" : \"tomorrow\",\n"
            + "  \"scope\" : \"read\",\n"
            + "  \"sub\" : \"radar_restapi\",\n"
            + "  \"sources\" : [ ],\n"
            + "  \"iss\" : \"ManagementPortal\",\n"
            + "  \"iat\" : " + tokenIssueDate + ",\n"
            + "  \"jti\" : \"" + accessTokenId + "\"\n"
            + "}";
    }

    private String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJyYWRhcl9yZXN0YXBpIiwi19NYW5hZ2VtZW50UG9ydGFsIl0sInNvdXJjZXMiOltdLCJzY29wZSI6WyJyZWFkIl0sImlzcyI6Ik1hbmFnZW1lbnleHAiOjE1MDQwODU3MzEsImlhdCI6MTUwNDA4MzkzMSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6TJmMTItNDQxMi1iZGVjLTc5YzMxNWY3NGM3OSIsImNsaWVudF9pZCI6InJhZGFyX3Jlc3RhcGkifQ.J0TEFQAUnH9RFaplURHrbeLelgbAr3CS7os_Y5S6836TFZyDe4mz4LqhxJLquXxTNP3DYddOKDD_RQ1t0nIDfx0hFJawPB3AjVqobRLOtFQWWdtYYmPbDXVQkdK41iVDhl_15BBxxOlT0pFQfkq4wk22ubq5cg8VZ57xDkrfgaIbdowntnK9GqLy6mDtaPdQV23VDr3whkjEq2YJ9AQBj4KiOWEVAYuNwhZFwHwInsYPZTs2RNK5WkdW2pe4sXGc7BDgUykpUWEMtL7BoyTZEGO5VqDkwcbio1zJDGB5dPm8VHWtlg4tH098BhsFrVE3zOJ9D0Ai62JWZkzr24lH9QjBwruxifyu4AvcLp_AxmO7m_r1bLcDuh6Yt4Ntm1bhGoB_PrygiOFPMn2-VnUH9zTxpZaKUH9CHHKOVdcK9N3gLKo30ETVDib-bZS-rDESHDvnYppgTH6i31wfjl80NCQhSpB3GyXAR2YHfoTj4VbEzGKsLEfS7g-4hSH2kY4-srOAH5TeI2snKbh76mFL8SOTuZrHf-F5KwWPqB82OzAr899eFk6uiNd5Uz7dICyEKyS7v-HQ";
    private String accessTokenId = "5b9fc645-2f12-4412-bdec-79c315f74c79";
    private long tokenIssueDate;

    private String invalidScopeResponse = "{\n"
        + "  \"error\" : \"invalid_scope\",\n"
        + "  \"error_description\" : \"Invalid scope: write\",\n"
        + "  \"scope\" : \"read\"\n"
        + "}\n";

    private String invalidCredentialsResponse = "{\n"
        + "  \"timestamp\" : \"2017-08-31T09:50:19.779+0000\",\n"
        + "  \"status\" : 401,\n"
        + "  \"error\" : \"Unauthorized\",\n"
        + "  \"message\" : \"Bad credentials\",\n"
        + "  \"path\" : \"/oauth/token\""
        + "}";

    private String invalidGrantTypeResponse = "{\n"
        + "  \"error\" : \"invalid_client\",\n"
        + "  \"error_description\" : \"Unauthorized grant type: client_credentials\"\n"
        + "}";

    private String notFoundResponse = "{\n"
        + "  \"timestamp\" : \"2017-08-31T12:00:56.274+0000\",\n"
        + "  \"status\" : 404,\n"
        + "  \"error\" : \"Not Found\",\n"
        + "  \"message\" : \"Not Found\",\n"
        + "  \"path\" : \"/oauth/token\"\n"
        + "}\n";
}
