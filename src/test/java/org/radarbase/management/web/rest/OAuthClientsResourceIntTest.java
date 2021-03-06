package org.radarbase.management.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.radarbase.management.service.OauthClientServiceTest.createClient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authentication.OAuthHelper;
import org.radarbase.management.ManagementPortalApp;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.OAuthClientService;
import org.radarbase.management.service.SubjectService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.ClientDetailsDTO;
import org.radarbase.management.service.mapper.ClientDetailsMapper;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class OAuthClientsResourceIntTest {

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ClientDetailsMapper clientDetailsMapper;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private OAuthClientService oAuthClientService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private HttpServletRequest servletRequest;

    private MockMvc restOauthClientMvc;

    private ClientDetailsDTO details;

    private List<ClientDetails> clientDetailsList;

    private int databaseSizeBeforeCreate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        OAuthClientsResource oauthClientsResource = new OAuthClientsResource();
        ReflectionTestUtils.setField(oauthClientsResource, "clientDetailsMapper",
                clientDetailsMapper);
        ReflectionTestUtils.setField(oauthClientsResource, "subjectService",
                subjectService);
        ReflectionTestUtils.setField(oauthClientsResource, "userService",
                userService);
        ReflectionTestUtils.setField(oauthClientsResource, "servletRequest",
                servletRequest);
        ReflectionTestUtils.setField(oauthClientsResource, "oAuthClientService",
                oAuthClientService);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restOauthClientMvc = MockMvcBuilders.standaloneSetup(oauthClientsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter(filter)
            .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();

        databaseSizeBeforeCreate = clientDetailsService.listClientDetails().size();

        // Create the OAuth Client
        details = createClient();
        restOauthClientMvc.perform(post("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isCreated());

        // Validate the Project in the database
        clientDetailsList = clientDetailsService.listClientDetails();
        assertThat(clientDetailsList).hasSize(databaseSizeBeforeCreate + 1);
    }

    @Test
    @Transactional
    public void createAndFetchOAuthClient() throws Exception {
        // fetch the created oauth client and check the json result
        restOauthClientMvc.perform(get("/api/oauth-clients/" + details.getClientId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(equalTo(details.getClientId())))
                .andExpect(jsonPath("$.clientSecret").value(nullValue()))
                .andExpect(jsonPath("$.accessTokenValiditySeconds").value(equalTo(details
                        .getAccessTokenValiditySeconds().intValue())))
                .andExpect(jsonPath("$.refreshTokenValiditySeconds").value(equalTo(details
                        .getRefreshTokenValiditySeconds().intValue())))
                .andExpect(jsonPath("$.scope").value(contains(details.getScope().toArray())))
                .andExpect(jsonPath("$.autoApproveScopes").value(contains(details
                        .getAutoApproveScopes().toArray())))
                .andExpect(jsonPath("$.authorizedGrantTypes").value(contains(details
                        .getAuthorizedGrantTypes().toArray())))
                .andExpect(jsonPath("$.authorities").value(
                        contains(details.getAuthorities().toArray())));

        ClientDetails testDetails = clientDetailsList.stream().filter(
                d -> d.getClientId().equals(details.getClientId())).findFirst().get();
        assertThat(testDetails.getClientSecret()).startsWith("$2a$10$");
        assertThat(testDetails.getScope()).containsExactlyElementsOf(details.getScope());
        assertThat(testDetails.getResourceIds()).containsExactlyElementsOf(
                details.getResourceIds());
        assertThat(testDetails.getAuthorizedGrantTypes()).containsExactlyElementsOf(
                details.getAuthorizedGrantTypes());
        details.getAutoApproveScopes().forEach(scope ->
                assertThat(testDetails.isAutoApprove(scope)).isTrue());
        assertThat(testDetails.getAccessTokenValiditySeconds()).isEqualTo(
                details.getAccessTokenValiditySeconds().intValue());
        assertThat(testDetails.getRefreshTokenValiditySeconds()).isEqualTo(
                details.getRefreshTokenValiditySeconds().intValue());
        assertThat(testDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                .containsExactlyElementsOf(details.getAuthorities());
        assertThat(testDetails.getAdditionalInformation()).containsAllEntriesOf(
                details.getAdditionalInformation()
        );
    }

    @Test
    @Transactional
    public void dupliceOAuthClient() throws Exception {
        restOauthClientMvc.perform(post("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void updateOAuthClient() throws Exception {
        // update the client
        details.setRefreshTokenValiditySeconds(20L);
        restOauthClientMvc.perform(put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isOk());

        // fetch the client
        clientDetailsList = clientDetailsService.listClientDetails();
        assertThat(clientDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        ClientDetails testDetails = clientDetailsList.stream().filter(
                d -> d.getClientId().equals(details.getClientId())).findFirst().get();
        assertThat(testDetails.getRefreshTokenValiditySeconds()).isEqualTo(20);
    }

    @Test
    @Transactional
    public void deleteOAuthClient() throws Exception {
        restOauthClientMvc.perform(delete("/api/oauth-clients/" + details.getClientId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isOk());
        clientDetailsList = clientDetailsService.listClientDetails();
        assertThat(clientDetailsList.size()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void cannotModifyProtected() throws Exception {
        // first change our test client to be protected
        details.getAdditionalInformation().put("protected", "true");
        restOauthClientMvc.perform(put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isOk());

        // expect we can not delete it now
        restOauthClientMvc.perform(delete("/api/oauth-clients/" + details.getClientId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isForbidden());

        // expect we can not update it now
        details.setRefreshTokenValiditySeconds(20L);
        restOauthClientMvc.perform(put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details)))
                .andExpect(status().isForbidden());
    }


}
