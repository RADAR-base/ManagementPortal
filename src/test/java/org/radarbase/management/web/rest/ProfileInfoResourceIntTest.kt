package org.radarbase.management.web.rest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Test class for the ProfileInfoResource REST controller.
 *
 * @see ProfileInfoResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
internal class ProfileInfoResourceIntTest(
    @Autowired private val profileInfoResource: ProfileInfoResource
) : BasePostgresIntegrationTest() {

    @Mock private lateinit var environment: Environment
    private lateinit var restProfileMockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val activeProfiles = arrayOf("test")
        Mockito.`when`(environment.defaultProfiles).thenReturn(activeProfiles)
        Mockito.`when`(environment.activeProfiles).thenReturn(activeProfiles)
        restProfileMockMvc = MockMvcBuilders
            .standaloneSetup(profileInfoResource)
            .build()
    }

    @Throws(Exception::class)
    @Test
    fun profileInfo() {
            restProfileMockMvc.perform(MockMvcRequestBuilders.get("/api/profile-info"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        }

    @Throws(Exception::class)
    @Test
    fun profileInfoWithoutActiveProfiles() {
            val emptyProfile = arrayOf<String>()
            Mockito.`when`(environment.defaultProfiles).thenReturn(emptyProfile)
            Mockito.`when`(environment.activeProfiles).thenReturn(emptyProfile)
            restProfileMockMvc.perform(MockMvcRequestBuilders.get("/api/profile-info"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        }
}
