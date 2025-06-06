package org.radarbase.management.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.annotation.Timed
import org.radarbase.management.domain.Query
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.*
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.ErrorVM
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/query-builder")
class QueryResource(
    @Autowired private val queryBuilderService: QueryBuilderService,
    @Autowired private var userRepository: UserRepository,
    @Autowired private val userService: UserService,
    @Autowired private val queryEValuationService: QueryEValuationService,
    @Autowired private val queryContentService: QueryContentService

) {
    @PostMapping("querylogic")
    fun saveQueryLogic(@RequestBody queryJson: String?): ResponseEntity<*> {
        if(queryJson.isNullOrEmpty() == false) {
                val objectMapper = jacksonObjectMapper()
                val queryLogicDTO: QueryLogicDTO = objectMapper.readValue(queryJson)
                queryBuilderService.processQueryLogicJson(queryLogicDTO);
            }
            return ResponseEntity.ok(null);
    }

    @PutMapping("querylogic")
    fun updateQueryLogic(@RequestBody queryJson: String?): ResponseEntity<*> {
        if(queryJson.isNullOrEmpty() == false) {
            val objectMapper = jacksonObjectMapper()
            val queryLogicDTO: QueryLogicDTO = objectMapper.readValue(queryJson)
            if(queryLogicDTO.queryGroupId != null) {
                queryBuilderService.deleteAllQueryLogic(queryLogicDTO.queryGroupId!!);
                queryBuilderService.processQueryLogicJson(queryLogicDTO);
            }

        }
        return ResponseEntity.ok(null);
    }

    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return when (val principal = authentication?.principal) {
            is UserDetails -> principal.username
            is String -> principal // If the principal is a string (e.g., anonymous user)
            else -> null
        }
    }

    @PostMapping("querygroups")
    fun createQueryGroup(@RequestBody queryJson: String?): ResponseEntity<Long?> {
        var queryGroupId: Long? = null
        if(queryJson.isNullOrEmpty() == false) {
            val objectMapper = jacksonObjectMapper()
            val queryGroupDTO: QueryGroupDTO = objectMapper.readValue(queryJson)
            val user = userService.getUserWithAuthorities()
            if(user != null) {
                queryGroupId = queryBuilderService.createQueryGroup(queryGroupDTO, user!!);
            }

        }
        return ResponseEntity.ok(queryGroupId);
    }

    @PutMapping("querygroups/{queryGroupId}")
    fun updateQueryGroup(@PathVariable("queryGroupId") id: Long, @RequestBody queryJson: String?): ResponseEntity<*> {
        if(!queryJson.isNullOrEmpty()) {
            val objectMapper = jacksonObjectMapper()
            val queryGroupDTO: QueryGroupDTO = objectMapper.readValue(queryJson)
            val user = userService.getUserWithAuthorities()
            if(user != null) {
                queryBuilderService.updateQueryGroup(id, queryGroupDTO, user!!);
            }

        }
        return ResponseEntity.ok(id)
    }

    //TODO: this will be eventually replaced by a worker
    @PostMapping("evaluate/{subjectId}")
    @Timed
    fun testLogicEvaluation(
        @PathVariable subjectId: Long?,
        @RequestBody userData: UserData?
    ): ResponseEntity<*> {
        return if(subjectId != null) {
            //TODO: get queryGroup based on assigned query once the PR for that is completed
            val result = queryEValuationService.testLogicEvaluation(subjectId, userData  );
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest()
                .body(ErrorVM(ErrorConstants.ERR_VALIDATION, "Subject ID or QueryGroupId is missing"))
        }
    }

    @GetMapping("queries")
    fun getQueryList(): ResponseEntity<MutableList<Query>> {
        var list = queryBuilderService.getQueryList()
        return ResponseEntity.ok(list)
    }

    @GetMapping("querygroups")
    fun getQueryGroupList(): ResponseEntity<MutableList<QueryGroup>> {
            var list = queryBuilderService.getQueryGroupList()
            return ResponseEntity.ok(list)
    }


    @GetMapping("querygroups/{id}")
    fun getQueryLogicTreeFromQueryGroup(@PathVariable("id") id: Long): ResponseEntity<AngularQueryBuilderDTO> {
        var list = queryBuilderService.buildQueryLogicTree(id);
        return ResponseEntity.ok(list)
    }

    @DeleteMapping("querygroups/{id}")
    fun deleteQueriesByID(@PathVariable("id") id: Long) {
            queryBuilderService.deleteAllRelatedByQueryGroupId(id)
    }

    @PostMapping("queryparticipant")
    fun assignQueryGroup(@RequestBody queryJson: String?): ResponseEntity<*> {
            var queryParticipantId: Long? = null

            if (queryJson.isNullOrEmpty() == false) {

                val objectMapper = jacksonObjectMapper()
                val queryParticipantDTO: QueryParticipantDTO = objectMapper.readValue(queryJson)
                val user = userService.getUserWithAuthorities()

                if (user != null) {
                    queryParticipantId = queryBuilderService.assignQueryGroup(queryParticipantDTO, user)
                }
            }
            return ResponseEntity.ok(queryParticipantId)
        }

    @GetMapping("querygroups/subject/{subjectId}")
    fun getAssignedQueries(@PathVariable subjectId: Long): ResponseEntity<*> {
            return ResponseEntity.ok(queryBuilderService.getAssignedQueryGroups(subjectId))
    }

    @DeleteMapping("querygroups/{queryGroupId}/subject/{subjectId}")
    fun deleteAssignedQueryGroup(@PathVariable subjectId: Long, @PathVariable queryGroupId: Long) {
            queryBuilderService.deleteQueryParticipantByQueryGroup(subjectId, queryGroupId)
    }

    @GetMapping("querycontent/active/{participantId}")
    fun getActiveQueryContentForParticipant(@PathVariable participantId: Long): ResponseEntity<*> {
        val result = queryEValuationService.getActiveQueryContentForParticipant(participantId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("querycontent/querygroup/{queryGroupId}")
    fun saveQueryContent(@PathVariable queryGroupId: Long, @RequestBody queryContentDTO: List<QueryContentDTO>): ResponseEntity<*> {
        queryContentService.saveAll(queryGroupId, queryContentDTO);
        return ResponseEntity.ok(null);
    }

    @GetMapping("querycontent/querygroup/{queryGroupId}")
    fun getQueryContent(@PathVariable queryGroupId: Long): ResponseEntity<*> {
        val result =  queryContentService.findAllByQueryGroupId(queryGroupId)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("queryevaluation/remove-content")
    fun deleteQueryEvaluationContent(
        @RequestParam queryGroupId: Long,
        @RequestParam subjectId: Long
    ): ResponseEntity<Void> {
        queryEValuationService.removeContentByQueryGroupAndSubject(queryGroupId, subjectId)
        return ResponseEntity.ok().build()
    }


    companion object {
        private val log = LoggerFactory.getLogger(QueryResource::class.java)
    }

}
