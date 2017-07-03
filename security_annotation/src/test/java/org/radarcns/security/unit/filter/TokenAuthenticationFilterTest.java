package org.radarcns.security.unit.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.security.annotation.Secured;
import org.radarcns.security.authorization.AuthorizationHandler;
import org.radarcns.security.authorization.RadarAuthorizationHandler;
import org.radarcns.security.filter.TokenAuthenticationFilter;
import org.radarcns.security.unit.util.TokenTestUtils;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dverbeec on 24/04/2017.
 */

public class TokenAuthenticationFilterTest {

    private TokenAuthenticationFilter filter;

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
                .withHeader(HttpHeaders.CONTENT_TYPE, TokenTestUtils.APPLICATION_JSON)
                .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));
        filter = new TokenAuthenticationFilter();
    }

    @Test
    public void testGetToken() {
        ContainerRequestContext ctx = mock(ContainerRequestContext.class);
        when(ctx.getHeaderString(HttpHeaders.AUTHORIZATION))
            .thenReturn("Bearer " + TokenTestUtils.VALID_TOKEN);
        assertEquals(TokenTestUtils.VALID_TOKEN, filter.getToken(ctx));
    }

    @Test
    public void testCheckScopesEmpty() {
        List<String> allowed = new ArrayList<>();
        List<String> tokenScopes = Arrays.asList("scope1","scope2");
        filter.checkTokenScope(allowed, tokenScopes);
    }

    @Test
    public void testCheckScopesAllowed() {
        List<String> allowed = Arrays.asList("scope3", "scope4","scope1");
        List<String> tokenScopes = Arrays.asList("scope1", "scope2");
        filter.checkTokenScope(allowed, tokenScopes);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testCheckScopesNotAllowed() {
        List<String> allowed = Arrays.asList("scope3", "scope4");
        List<String> tokenScopes = Arrays.asList("scope1", "scope2");
        filter.checkTokenScope(allowed, tokenScopes);
    }

    @Test
    public void testExtractScopes() {
        String[] scopes = {"scope1", "scope2"};
        Secured secured = mock(Secured.class);
        when(secured.scopesAllowed()).thenReturn(scopes);
        AnnotatedElement element = mock(AnnotatedElement.class);
        when(element.getAnnotation(Secured.class)).thenReturn(secured);
        List<String> extracted = filter.extractScopes(element);
        List<String> expected = Arrays.asList(scopes);

        // test lists contain the same elements, disregarding order of items
        assertEquals(expected.size(), extracted.size());
        for (String scope : expected) {
            assertTrue(extracted.contains(scope));
        }
    }

    @Test
    public void testCreateSecurityContext() throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException {
        SecurityContext context = filter.createSecurityContext(TokenTestUtils.USER,
            Arrays.asList(TokenTestUtils.ROLES));

        assertEquals(TokenTestUtils.USER, context.getUserPrincipal().getName());
        for(String role : TokenTestUtils.ROLES) {
            assertTrue(context.isUserInRole(role));
        }

        assertFalse(context.isUserInRole("NO_PROJECT:NO_ROLE"));
    }
}
