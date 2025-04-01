package org.radarbase.management.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.QueryBuilderService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.QueryParticipantDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/query-builder")
class QueryResource(
    @Autowired private val queryBuilderService: QueryBuilderService,
    @Autowired private var userRepository: UserRepository,
    @Autowired private val userService: UserService

) {
    @PostMapping("query-logic")
    fun saveQueryLogic(@RequestBody queryJson: String?): ResponseEntity<*> {
            if(queryJson.isNullOrEmpty() == false) {
                val objectMapper = jacksonObjectMapper()
                val queryLogicDTO: QueryLogicDTO = objectMapper.readValue(queryJson)
                queryBuilderService.processQueryLogicJson(queryLogicDTO);
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
        @PostMapping("query-group")
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

    @GetMapping("querygroups")
    fun getQueryGroupList(): ResponseEntity<MutableList<QueryGroup>> {
        var list =  queryBuilderService.getQueryGroupList();

        return  ResponseEntity.ok(list);
    }

    @PostMapping("assignQueryGroup")
    fun assignQueryGroup(@RequestBody queryJson: String?):ResponseEntity<*>{
        var queryParticipantId: Long? = null

        if(queryJson.isNullOrEmpty() == false) {

            val objectMapper = jacksonObjectMapper()
            val queryParticipantDTO: QueryParticipantDTO = objectMapper.readValue(queryJson)
            val user = userService.getUserWithAuthorities()

            if(user != null) {
                queryParticipantId = queryBuilderService.assignQueryGroup(queryParticipantDTO)
            }
        }
        return ResponseEntity.ok(queryParticipantId)
    }

    @GetMapping("getAssignedQueryGroups")
    fun getAssignedQueries(@RequestParam subjectID:Long):ResponseEntity<*>{
        return ResponseEntity.ok(queryBuilderService.getAssignedQueryGroups(subjectID))
    }

    @DeleteMapping("deleteAssignedQueryGroup/{subjectid}/{querygroupid}")
    fun deleteAssignedQueryGroup(@PathVariable("subjectid")subjectID: Long, @PathVariable("querygroupid")querygroupid:Long){
            queryBuilderService.deleteQueryParticipantByQueryGroup(subjectID,querygroupid)
    }


    companion object {
        private val log = LoggerFactory.getLogger(QueryResource::class.java)
        private const val ENTITY_NAME = "query"
    }

}
