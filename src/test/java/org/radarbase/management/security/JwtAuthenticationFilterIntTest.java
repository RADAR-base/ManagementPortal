package org.radarbase.management.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.service.ProjectService;
import org.radarbase.auth.authentication.OAuthHelper;
import org.radarbase.management.web.rest.ProjectResource;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the JwtAuthenticationFilter class.
 *
 * @see JwtAuthenticationFilter
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class JwtAuthenticationFilterIntTest {

    @Autowired
    private RadarToken radarToken;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    private MockMvc rsaRestProjectMockMvc;

    private MockMvc ecRestProjectMockMvc;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        ProjectResource projectResource = new ProjectResource();
        ReflectionTestUtils.setField(projectResource, "projectService", projectService);
        ReflectionTestUtils.setField(projectResource, "token", radarToken);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.rsaRestProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.rsaBearerToken())).build();

        this.ecRestProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }

    @Test
    void testMultipleSigningKeys() throws Exception {
        // Check that we can get the project list with both RSA and EC signed token. We are testing
        // acceptance of the tokens, so no test on the content of the response is performed here.
        rsaRestProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
                .andExpect(status().isOk());
        ecRestProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
                .andExpect(status().isOk());
    }
}
