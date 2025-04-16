package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.QueryParticipant
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.repository.*
import org.radarbase.management.service.QueryBuilderService
import org.radarbase.management.service.QueryServiceTest
import org.radarbase.management.service.QueryServiceTest.Companion
import org.radarbase.management.service.QueryServiceTest.Companion.createQueryDTO
import org.radarbase.management.service.SubjectServiceTest
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import javax.servlet.ServletException

/**
 * Test class for the QueryResource REST controller.
 *
 * @see QueryResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class QueryResourceIntTest(
         @Autowired private val queryResource: QueryResource,
         @Autowired private val queryBuilderService: QueryBuilderService,
         @Autowired private val userService: UserService,
         @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
         @Autowired private val exceptionTranslator: ExceptionTranslator,
         @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
         @Autowired private val queryLogicRepository: QueryLogicRepository,
         @Autowired private val userRepository: UserRepository,
        @Autowired private val queryParticipantRepository: QueryParticipantRepository,
        @Autowired private val queryGroupRepository: QueryGroupRepository,
         @Autowired private val subjectRepository: SubjectRepository

) {
    private lateinit var restQueryMockMvc: MockMvc
    private lateinit var queryParticipant: QueryParticipant

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restQueryMockMvc =
            MockMvcBuilders.standaloneSetup(queryResource).setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator).setMessageConverters(jacksonMessageConverter)
                .addFilter<StandaloneMockMvcBuilder>(filter)
                .defaultRequest<StandaloneMockMvcBuilder>(
                    MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken())
                ).build()
    }

    @Test
    @Throws(Exception::class)
    fun saveQueryLogic() {
        val databaseSizeBeforeCreate = queryLogicRepository.findAll().size
        val query = createQueryDTO(QueryMetric.HEAR_RATE, ComparisonOperator.LESS_THAN, "60", QueryTimeFrame.LESS_THAN_OR_EQUALS)

        val queryLogicDto: QueryLogicDTO = QueryServiceTest.createQueryLogicDTOWithQuery(query)
        restQueryMockMvc.perform(MockMvcRequestBuilders.post("/api/query-builder/query-logic").contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queryLogicDto)))


        val queryLogicList = queryLogicRepository.findAll()

        Assertions.assertThat(queryLogicList).hasSize(databaseSizeBeforeCreate+1)
    }

    @Test
    @Throws(Exception::class)
    fun createQueryGroup(){
        val databaseSizeBeforeCreate = queryLogicRepository.findAll().size

        val queryGroupDTO = this.createQueryGroupDTO()

        restQueryMockMvc.perform(MockMvcRequestBuilders.post("/api/query-builder/query-group").contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queryGroupDTO))).andExpect(MockMvcResultMatchers.status().isOk())


        val queryLogicList = queryLogicRepository.findAll()

        Assertions.assertThat(queryLogicList).hasSize(databaseSizeBeforeCreate+1)
    }

    fun createQueryGroupDTO(): QueryGroupDTO{
        val queryGroupDTO = QueryGroupDTO();
        queryGroupDTO.name = "QueryGroup"
        queryGroupDTO.description = "This is description"

        return queryGroupDTO
    }

    @Test
    @Throws(Exception::class)
    fun getQueryGroupList(){
        val queryGroupDTO = this.createQueryGroupDTO()
        val user = userService.getUserWithAuthorities()
        if (user != null) {
            queryBuilderService.createQueryGroup(queryGroupDTO,user)
        }

        restQueryMockMvc.perform(MockMvcRequestBuilders.get("/api/query-builder/querygroups"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

    }

    @Test
    @Throws(Exception::class)
    fun assignQueryGroup(){
        val databaseSizeBeforeCreate = queryParticipantRepository.findAll().size

        val groupId =  QueryServiceTest.createQueryGroup(userRepository, queryBuilderService);

        val queryParticipantDTO = QueryServiceTest.createQueryParticipantDTO(groupId, 1)

        restQueryMockMvc.perform(MockMvcRequestBuilders.post("/api/query-builder/query-group").contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queryParticipantDTO))).andExpect(MockMvcResultMatchers.status().isOk())


        val queryLogicList = queryLogicRepository.findAll()

        Assertions.assertThat(queryLogicList).hasSize(databaseSizeBeforeCreate+1)
    }

    @Test
    @Throws(Exception::class)
    fun getAssignedQueries(){
        val id = QueryServiceTest.createQueryGroup(userRepository, queryBuilderService);


        val queryParticipant = QueryServiceTest.createQueryParticipantDTO(id!!, 1)

        queryBuilderService.assignQueryGroup(queryParticipant)


        restQueryMockMvc.perform(MockMvcRequestBuilders.get("/api/query-builder/querygroups/subject/{subjectId}", 1))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    @Throws(Exception::class)
    fun deleteAssignedQueryGroup(){
        queryParticipant = createQueryParticipant()
        queryParticipantRepository.saveAndFlush(queryParticipant)

        restQueryMockMvc.perform( MockMvcRequestBuilders.delete("/api/query-builder/querygroups/{subjectId}/subject/{queryGroupId}",
            queryParticipant.subject?.id ,queryParticipant.queryGroup?.id)
            .accept(TestUtil.APPLICATION_JSON_UTF8)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

    }

    fun createQueryParticipant(): QueryParticipant{
        val qp = QueryParticipant()

        val groupId = QueryServiceTest.createQueryGroup(userRepository, queryBuilderService)

        qp.queryGroup = queryGroupRepository.findById(groupId).get()

        qp.subject = subjectRepository.findById(1).get()

        return  qp
    }




}
