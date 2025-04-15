package org.radarbase.management.service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.QueryLogicType
import org.radarbase.management.repository.*
import org.radarbase.management.service.dto.QueryDTO
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.QueryParticipantDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.ZonedDateTime
import javax.persistence.EntityNotFoundException


@Service
@Transactional
public class QueryBuilderService(
    private val queryLogicRepository:  QueryLogicRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryRepository: QueryRepository,
    private var userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    @Autowired private val userService: UserService,
    private var queryParticipantRepository: QueryParticipantRepository
) {

    @Transactional
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

    fun getQueryList(): MutableList<Query> {
        return queryRepository.findAll()
    }

    @Transactional
    fun deleteAllRelatedByQueryGroupId(queryGroupId: Long) {

        val group = queryGroupRepository.findById(queryGroupId)
            .orElseThrow { EntityNotFoundException("QueryGroup with id=$queryGroupId not found") }

        queryGroupRepository.delete(group)
    }


    fun getQueryGroupList(): MutableList<QueryGroup> {
        return queryGroupRepository.findAll();
    }

    fun assignQueryGroup(queryParticipantDTO: QueryParticipantDTO): Long?{
        var queryParticipant = QueryParticipant()
        val queryGroup = queryGroupRepository.findById(queryParticipantDTO.queryGroupId!!).get()
        val user = userService.getUserWithAuthorities()

        queryParticipant.queryGroup= queryGroup
        queryParticipant.createdBy= user
        queryParticipant.subject = subjectRepository.findById(queryParticipantDTO.subjectId!!).get();
        queryParticipant.createdDate = ZonedDateTime.now();
        queryParticipant = queryParticipantRepository.save(queryParticipant)
        queryGroupRepository.flush()

        return queryParticipant.id;
    }

    fun getAssignedQueryGroups(subjectId: Long): MutableList<QueryGroup> {
        var queryParticipantList =  queryParticipantRepository.findBySubjectId(subjectId)

        var queryGroups = mutableListOf<QueryGroup>()

        for(queryParticipant in queryParticipantList ){
            var group =queryParticipant.queryGroup
            if (group != null) {
                queryGroups.add(group)
            }
        }
        return queryGroups;
    }

    fun deleteQueryParticipantByQueryGroup(subjectId: Long, queryGroupId: Long) {
        queryParticipantRepository.delete(
            queryParticipantRepository.findBySubjectIdAndQueryGroupId(
                subjectId,
                queryGroupId
            )
        )

    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}

