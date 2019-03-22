package org.radarcns.management.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.service.ProjectService;
import org.radarcns.auth.authentication.OAuthHelper;
import org.radarcns.management.web.rest.ProjectResource;
import org.radarcns.management.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
public class JwtAuthenticationFilterIntTest {

    @Autowired
    private HttpServletRequest servletRequest;

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

    @Before
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        ProjectResource projectResource = new ProjectResource();
        ReflectionTestUtils.setField(projectResource, "projectService", projectService);
        ReflectionTestUtils.setField(projectResource, "servletRequest", servletRequest);

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
    public void testMultipleSigningKeys() throws Exception {
        // Check that we can get the project list with both RSA and EC signed token. We are testing
        // acceptance of the tokens, so no test on the content of the response is performed here.
        rsaRestProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
                .andExpect(status().isOk());
        ecRestProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
                .andExpect(status().isOk());
    }
}
