package org.radarbase.management.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.QueryBuilderService
import org.radarbase.management.service.QueryContentService
import org.radarbase.management.service.QueryEValuationService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api")
class QueryContentResource(


    @Autowired private val queryContentService: QueryContentService,
) {


    @PostMapping("querycontent/querygroup/{queryGroupId}")
    fun saveQueryContent(@PathVariable queryGroupId: Long, @RequestBody queryContentDTO: List<QueryContentDTO>): ResponseEntity<*> {

        log.info("[QUERY-CONTENT] query group id is {} and the size of list is {}", queryGroupId, queryContentDTO.size)
        log.info("[QUERY-CONTENT] and the DTO is {}",  queryContentDTO)
        queryContentService.saveAll(queryGroupId, queryContentDTO);
        return ResponseEntity.ok(null);
    }

    @GetMapping("querycontent/querygroup/{queryGroupId}")
    fun getQueryContent(@PathVariable queryGroupId: Long): ResponseEntity<*> {
      val result =  queryContentService.findAllByQueryGroupId(queryGroupId)

        return ResponseEntity.ok(result)
    }


    companion object {
        private val log = LoggerFactory.getLogger(QueryContentResource::class.java)
    }
}
