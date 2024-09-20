package org.radarbase.management.web.rest

import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.audit.AuditEventConverter
import org.radarbase.management.domain.PersistentAuditEvent
import org.radarbase.management.repository.PersistenceAuditEventRepository
import org.radarbase.management.service.AuditEventService
import org.radarbase.management.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.ServletException

/**
 * Test class for the AuditResource REST controller.
 *
 * @see AuditResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
internal class AuditResourceIntTest(
    @Autowired private val auditEventRepository: PersistenceAuditEventRepository,
    @Autowired private val auditEventConverter: AuditEventConverter,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val formattingConversionService: FormattingConversionService,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val authService: AuthService,
) {
    private lateinit var auditEvent: PersistentAuditEvent
    private lateinit var restAuditMockMvc: MockMvc

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val auditEventService =
            AuditEventService(
                auditEventRepository,
                auditEventConverter,
            )
        val auditResource = AuditResource(auditEventService, authService)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restAuditMockMvc =
            MockMvcBuilders
                .standaloneSetup(auditResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setConversionService(formattingConversionService)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter<StandaloneMockMvcBuilder>(filter)
                .defaultRequest<StandaloneMockMvcBuilder>(
                    MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()),
                )
                .build()
    }

    @BeforeEach
    fun initTest() {
        auditEventRepository.deleteAll()
        auditEvent = PersistentAuditEvent()
        auditEvent.auditEventType = SAMPLE_TYPE
        auditEvent.principal = SAMPLE_PRINCIPAL
        auditEvent.auditEventDate = SAMPLE_TIMESTAMP
    }

    @Throws(Exception::class)
    @Test
    fun allAudits() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Get all the audits
        restAuditMockMvc
            .perform(MockMvcRequestBuilders.get("/management/audits"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].principal").value<Iterable<String?>>(
                    Matchers.hasItem(
                        SAMPLE_PRINCIPAL,
                    ),
                ),
            )
    }

    @Throws(Exception::class)
    @Test
    fun audit() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Get the audit
        restAuditMockMvc
            .perform(MockMvcRequestBuilders.get("/management/audits/{id}", auditEvent.id))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.principal").value(SAMPLE_PRINCIPAL))
    }

    @Throws(Exception::class)
    @Test
    fun auditsByDate() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Generate dates for selecting audits by date, making sure the period contains the audit
        val fromDate = SAMPLE_TIMESTAMP.minusDays(1).format(FORMATTER)
        val toDate = SAMPLE_TIMESTAMP.plusDays(1).format(FORMATTER)

        // Get the audit
        restAuditMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/management/audits?fromDate=" + fromDate + "&toDate=" +
                        toDate,
                ),
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].principal").value<Iterable<String?>>(
                    Matchers.hasItem(
                        SAMPLE_PRINCIPAL,
                    ),
                ),
            )
    }

    @Throws(Exception::class)
    @Test
    fun nonExistingAuditsByDate() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Generate dates for selecting audits by date, making sure the period will not contain the
        // sample audit
        val fromDate = SAMPLE_TIMESTAMP.minusDays(2).format(FORMATTER)
        val toDate = SAMPLE_TIMESTAMP.minusDays(1).format(FORMATTER)

        // Query audits but expect no results
        restAuditMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/management/audits?fromDate=" + fromDate + "&toDate=" +
                        toDate,
                ),
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.header().string("X-Total-Count", "0"))
    }

    @Throws(Exception::class)
    @Test
    fun nonExistingAudit() {
        // Get the audit
        restAuditMockMvc
            .perform(MockMvcRequestBuilders.get("/management/audits/{id}", Long.MAX_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    companion object {
        private const val SAMPLE_PRINCIPAL = "SAMPLE_PRINCIPAL"
        private const val SAMPLE_TYPE = "SAMPLE_TYPE"
        private val SAMPLE_TIMESTAMP = LocalDateTime.parse("2015-08-04T10:11:30")
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
