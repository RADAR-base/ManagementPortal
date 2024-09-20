package org.radarbase.management.web.rest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.web.rest.vm.LoggerVM
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Test class for the LogsResource REST controller.
 *
 * @see LogsResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
internal class LogsResourceIntTest {
    private var restLogsMockMvc: MockMvc? = null

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val logsResource = LogsResource()
        restLogsMockMvc =
            MockMvcBuilders
                .standaloneSetup(logsResource)
                .build()
    }

    @Throws(Exception::class)
    @Test
    fun allLogs() {
        restLogsMockMvc!!
            .perform(MockMvcRequestBuilders.get("/management/logs"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    @Throws(Exception::class)
    fun changeLogs() {
        val logger = LoggerVM()
        logger.level = "INFO"
        logger.name = "ROOT"
        restLogsMockMvc!!
            .perform(
                MockMvcRequestBuilders
                    .put("/management/logs")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(logger)),
            ).andExpect(MockMvcResultMatchers.status().isNoContent())
    }
}
