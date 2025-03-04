package org.radarbase.management.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.Authority
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryLogicOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.repository.QueryLogicRepository
import org.radarbase.management.repository.QueryRepository
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.repository.filters.UserFilter
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.QueryDTO
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.web.rest.TestUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import tech.jhipster.service.QueryService
import java.time.Period
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
class QueryServiceTest(
    @Autowired private val queryService: QueryBuilderService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val queryGroupRepository: QueryGroupRepository,
    @Autowired private val queryLogicRepository: QueryLogicRepository




) {
    private lateinit var entityManager: EntityManager
    private lateinit var userDto: UserDTO

    @BeforeEach
    fun setUp() {

    }

    @Test
    fun assertThatCreateQueryGroupReturnsId() {
        val sizeBefore = queryGroupRepository.findAll().size

        val queryGroupDTO = QueryGroupDTO();
        queryGroupDTO.name = "QueryGroup"
        queryGroupDTO.description = "This is description"

        val user = userRepository.findOneByLogin("admin");

        val id = queryService.createQueryGroup(queryGroupDTO, user!!);
        Assertions.assertThat(id).isNotNull()
        Assertions.assertThat(sizeBefore + 1).isEqualTo(queryGroupRepository.findAll().size)


        val savedQueryGroup = queryGroupRepository.findById(id!!).get()
        Assertions.assertThat(savedQueryGroup.name).isEqualTo("QueryGroup")
        Assertions.assertThat(savedQueryGroup.description).isEqualTo("This is description")
        Assertions.assertThat(savedQueryGroup.createdBy!!.login).isEqualTo("admin")
    }

    @Test
    fun processQueryLogicJson() {
        val sizeBefore = queryLogicRepository.findAll().size

        val queryGroupDTO = QueryGroupDTO();
        queryGroupDTO.name = "QueryGroup"
        queryGroupDTO.description = "This is description"

        val user = userRepository.findOneByLogin("admin");

        val id = queryService.createQueryGroup(queryGroupDTO, user!!);

        val queryLogicDTO = QueryLogicDTO();
        queryLogicDTO.queryGroupId = id;
        queryLogicDTO.logicOperator = QueryLogicOperator.AND

        queryLogicDTO.children = listOf()

        val query = createQueryDTO(QueryMetric.HEAR_RATE, ComparisonOperator.LESS_THAN, "60", QueryTimeFrame.LESS_THAN_OR_EQUALS)
        val query1 = createQueryDTO(QueryMetric.HEAR_RATE, ComparisonOperator.GREATER_THAN, "70", QueryTimeFrame.LESS_THAN_OR_EQUALS)

        val queryLogicDTO1 = createQueryLogicDTOWithQuery(query)
        val queryLogicDTO2 = createQueryLogicDTOWithQuery(query1)

        queryLogicDTO.children = queryLogicDTO.children!! + queryLogicDTO1
        queryLogicDTO.children = queryLogicDTO.children!! + queryLogicDTO2

        queryService.processQueryLogicJson(queryLogicDTO)

        Assertions.assertThat(queryLogicRepository.findAll().size).isEqualTo(3)
    }


    /**
     * Create an expired user, save it and return the saved object.
     * @param userRepository The UserRepository that will be used to save the object
     * @return the saved object
     */


    companion object {
        fun createQueryDTO(queryMetric: QueryMetric, comparisonOperator: ComparisonOperator, value: String, queryTimeFrame: QueryTimeFrame): QueryDTO  {
            val query = QueryDTO();
            query.queryMetric = queryMetric
            query.comparisonOperator = comparisonOperator
            query.value = "50"
            query.timeFrame = queryTimeFrame
            return query
        }

    fun createQueryLogicDTOWithQuery(queryDTO: QueryDTO) : QueryLogicDTO{
            val queryLogicDTO = QueryLogicDTO()
            queryLogicDTO.query = queryDTO;
            return queryLogicDTO;
        }




    }
}
