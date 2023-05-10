package org.radarbase.management.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.radarbase.management.ManagementPortalTestApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the ProfileInfoResource REST controller.
 *
 * @see ProfileInfoResource
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
class ProfileInfoResourceIntTest {

    @Mock
    private Environment environment;

    private MockMvc restProfileMockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        String[] activeProfiles = {"test"};
        when(environment.getDefaultProfiles()).thenReturn(activeProfiles);
        when(environment.getActiveProfiles()).thenReturn(activeProfiles);

        ProfileInfoResource profileInfoResource = new ProfileInfoResource();
        ReflectionTestUtils.setField(profileInfoResource, "env", environment);
        this.restProfileMockMvc = MockMvcBuilders
                .standaloneSetup(profileInfoResource)
                .build();
    }

    @Test
    void getProfileInfo() throws Exception {
        restProfileMockMvc.perform(get("/api/profile-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getProfileInfoWithoutActiveProfiles() throws Exception {
        String[] emptyProfile = {};
        when(environment.getDefaultProfiles()).thenReturn(emptyProfile);
        when(environment.getActiveProfiles()).thenReturn(emptyProfile);

        restProfileMockMvc.perform(get("/api/profile-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
