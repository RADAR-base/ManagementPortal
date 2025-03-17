package org.radarbase.management.service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.radarbase.management.domain.Query
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.QueryLogic
import org.radarbase.management.domain.User
import org.radarbase.management.domain.enumeration.QueryLogicType
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.repository.QueryLogicRepository
import org.radarbase.management.repository.QueryRepository
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.dto.QueryDTO
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.ZonedDateTime


@Service
@Transactional
public class QueryBuilderService(
    private val queryLogicRepository:  QueryLogicRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryRepository: QueryRepository,
    private var userRepository: UserRepository
) {

    fun saveQuery(queryGroup: QueryGroup, query: QueryDTO): Query {
        var newQuery = Query()

        newQuery.queryGroup = queryGroup
        newQuery.metric = query.metric
        newQuery.operator = query.operator
        newQuery.value = query.value
        newQuery.time_frame = query.time_frame

        newQuery = queryRepository.save(newQuery);
        queryRepository.flush();

        return newQuery;
    }

    @Transactional
    fun saveQueryConditions(queryGroup: QueryGroup, parentId: Long?, dto: QueryLogicDTO) {
        val parent = parentId?.let { queryLogicRepository.findById(it).orElse(null) }

        var query : Query? = null
        if(dto.query != null) {
            query = saveQuery(queryGroup,dto.query!!)
        }

        val queryLogic = QueryLogic()
        queryLogic.queryGroup = queryGroup
        queryLogic.parent = parent
        queryLogic.type = if (dto.logic_operator != null)  QueryLogicType.LOGIC else QueryLogicType.CONDITION
        queryLogic.logic_operator = dto.logic_operator
        queryLogic.query = query

        val savedCondition = queryLogicRepository.save(queryLogic)
        queryLogicRepository.flush()

        dto.children?.forEach {
            saveQueryConditions(queryGroup, savedCondition.id, it)
        }
    }

    @Transactional
    fun processQueryLogicJson(queryLogicDTO: QueryLogicDTO){
        if(queryLogicDTO.queryGroupId != null) {
            val queryGroup = queryGroupRepository.findById(queryLogicDTO.queryGroupId!!).get()
            this.saveQueryConditions(queryGroup, null, queryLogicDTO)
        }
    }


    @Throws(IOException::class)
    fun createQueryGroup(queryGroupDTO: QueryGroupDTO, user : User): Long? {
        var queryGroup = QueryGroup();
        queryGroup.name  = queryGroupDTO.name
        queryGroup.description = queryGroupDTO.description;
        queryGroup.createdBy = user;
        queryGroup.createdDate  = ZonedDateTime.now()

        queryGroup = queryGroupRepository.save(queryGroup)
        queryGroupRepository.flush();

        return queryGroup.id;

    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}
