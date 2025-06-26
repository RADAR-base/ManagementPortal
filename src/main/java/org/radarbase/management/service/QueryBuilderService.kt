package org.radarbase.management.service
import io.ktor.util.*
import org.radarbase.management.domain.*
import org.radarbase.management.domain.Query
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.QueryLogic
import org.radarbase.management.domain.User
import org.radarbase.management.domain.enumeration.QueryLogicType
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.repository.QueryLogicRepository
import org.radarbase.management.repository.QueryRepository
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.repository.*
import org.radarbase.management.service.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.ZonedDateTime
import java.util.*
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
    private var queryParticipantRepository: QueryParticipantRepository,
    private val queryContentGroupRepository: QueryContentGroupRepository,
    private val queryContentRepository: QueryContentRepository
) {

    @Transactional
    fun saveQuery(queryGroup: QueryGroup, query: QueryDTO): Query {
        var newQuery = Query()

        newQuery.queryGroup = queryGroup
        newQuery.field = query.field
        newQuery.operator = query.operator
        newQuery.value = query.value
        newQuery.timeFrame = query.timeFrame
        newQuery.entity = query.entity;

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
        queryLogic.logicOperator = dto.logic_operator
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


    @Transactional
    fun deleteAllQueryLogic(queryGroupId: Long) {
        val queryLogicList = queryLogicRepository.findByQueryGroupId(queryGroupId)
        queryLogicRepository.deleteAll(queryLogicList);
        queryLogicRepository.flush();

        val queryList = queryRepository.findByQueryGroupId(queryGroupId);
        queryRepository.deleteAll(queryList);
        queryRepository.flush()
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

    @Throws(IOException::class)
    fun updateQueryGroup(queryGroupId: Long, queryGroupDTO: QueryGroupDTO, user : User) {
        val existingQueryGroupOpt = queryGroupRepository.findById(queryGroupId);

        if(existingQueryGroupOpt.isPresent) {
            val existingQueryGroup = existingQueryGroupOpt.get();

            existingQueryGroup.name = queryGroupDTO.name
            existingQueryGroup.description = queryGroupDTO.description
            existingQueryGroup.updatedDate  = ZonedDateTime.now()
            existingQueryGroup.updateBy = user;

            queryGroupRepository.saveAndFlush(existingQueryGroup);
        }
        else {
           throw  EntityNotFoundException("QueryGroup with id=$queryGroupId not found")
        }
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

    fun assignQueryGroup(queryParticipantDTO: QueryParticipantDTO, user: User): Long?{
        var queryParticipant = QueryParticipant()
        val queryGroup = queryGroupRepository.findById(queryParticipantDTO.queryGroupId!!).get()

        queryParticipant.queryGroup= queryGroup
        queryParticipant.createdBy= user
        queryParticipant.subject = subjectRepository.findById(queryParticipantDTO.subjectId!!).get();
        queryParticipant.createdDate = ZonedDateTime.now();
        queryParticipant = queryParticipantRepository.save(queryParticipant)
        queryGroupRepository.flush()

        return queryParticipant.id;
    }

    fun getAssignedQueryGroups(subjectId: Long): MutableList<QueryGroup> {
        val queryParticipantList =  queryParticipantRepository.findBySubjectId(subjectId)

        val queryGroups = mutableListOf<QueryGroup>()

        for(queryParticipant in queryParticipantList ){
            val group =queryParticipant.queryGroup
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


    fun buildQueryLogicTree(queryGroupId: Long): AngularQueryBuilderDTO?  {
        val queryLogicList = queryLogicRepository.findByQueryGroupId(queryGroupId);

        val conditionMap = queryLogicList.associateBy { it.id }

        data class QueryLogicDTOBuilder (
            val entity: QueryLogic,
            val children: MutableList<QueryLogicDTOBuilder> = mutableListOf()
        )

        val dtoMap = mutableMapOf<Long, QueryLogicDTOBuilder>()


        // first pass: wrap entities in builders
        queryLogicList.forEach { entity ->
            dtoMap[entity.id!!] = QueryLogicDTOBuilder(entity)
        }

        // second pass: attach children to parents

        queryLogicList.forEach { entity ->
        val parentId = entity.parent?.id
            if(parentId != null) {
                dtoMap[parentId]?.children?.add(dtoMap[entity.id]!!)
            }
        }

        val rootBuilder = dtoMap.values.find { it.entity.parent == null} ?: return null


        fun toDto(builder: QueryLogicDTOBuilder) : AngularQueryBuilderDTO {
            val queryLogicDTO = AngularQueryBuilderDTO()
            val query = builder.entity.query
            queryLogicDTO.condition = builder.entity.logicOperator.toString().lowercase(Locale.getDefault())
            queryLogicDTO.field = query?.field.toString().lowercase(Locale.getDefault())
            queryLogicDTO.operator = query?.operator?.symbol
            queryLogicDTO.timeFame = query?.timeFrame?.symbol
            queryLogicDTO.value = query?.value
            queryLogicDTO.entity = query?.entity

            if(builder.children.size > 0) {
                queryLogicDTO.rules = builder.children.map { toDto(it) }
            }
            queryLogicDTO.type = builder.entity.type

            return queryLogicDTO

        }

        val resultDto = toDto(rootBuilder)

        resultDto.queryGroupName = rootBuilder.entity.queryGroup?.name
        resultDto.queryGroupDescription = rootBuilder.entity.queryGroup?.description

        return resultDto
    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}

