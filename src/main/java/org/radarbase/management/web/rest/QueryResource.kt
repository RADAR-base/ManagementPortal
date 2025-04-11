package org.radarbase.management.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.annotation.Timed
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.QueryBuilderService
import org.radarbase.management.service.QueryEValuationService
import org.radarbase.management.service.UserData
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.ErrorVM
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException.BadRequest

@RestController
@RequestMapping("/api/query-builder")
class QueryResource(
    @Autowired private val queryBuilderService: QueryBuilderService,
    @Autowired private var userRepository: UserRepository,
    @Autowired private val userService: UserService,
    @Autowired private val queryEValuationService: QueryEValuationService

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


    //TODO: this will be eventually replaced by a worker
    @PostMapping("query/evaluate/{subjectId}/querygroup/{queryGroupId}")
    @Timed
    fun testLogicEvaluation(
        @PathVariable subjectId: Long?,
        @PathVariable queryGroupId: Long?,
        @RequestBody userData: UserData?
    ): ResponseEntity<*> {
        return if(subjectId != null && queryGroupId != null) {
            //TODO: get queryGroup based on assigned query once the PR for that is completed
            val result = queryEValuationService.testLogicEvaluation(queryGroupId, subjectId, userData  );
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest()
                .body(ErrorVM(ErrorConstants.ERR_VALIDATION, "Subject ID or QueryGroupId is missing"))
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryResource::class.java)
    }

}
