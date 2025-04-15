package org.radarbase.management.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.radarbase.management.domain.Query
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.service.QueryBuilderService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.QueryParticipantDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(QueryResource::class)
class QueryResourceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var queryBuilderService: QueryBuilderService

    @MockBean
    private lateinit var userService: UserService

    private val objectMapper = ObjectMapper()

    @Test
    fun shouldSaveQueryLogic() {
        val queryLogicDTO = QueryLogicDTO()
        val json = objectMapper.writeValueAsString(queryLogicDTO)

        mockMvc.perform(post("/api/query-builder/query-logic")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldCreateQueryGroup() {
        val queryGroupDTO = QueryGroupDTO()
        val json = objectMapper.writeValueAsString(queryGroupDTO)

        Mockito.`when`(userService.getUserWithAuthorities()).thenReturn(null)

        mockMvc.perform(post("/api/query-builder/query-group")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldGetQueryList() {
        Mockito.`when`(queryBuilderService.getQueryList()).thenReturn(mutableListOf<Query>())

        mockMvc.perform(get("/api/query-builder/queries"))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldGetQueryGroupList() {
        Mockito.`when`(queryBuilderService.getQueryGroupList()).thenReturn(mutableListOf<QueryGroup>())

        mockMvc.perform(get("/api/query-builder/querygroups"))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldDeleteQueryByID() {
        mockMvc.perform(delete("/api/query-builder/query/1"))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldAssignQueryQarticipant() {
        val dto = QueryParticipantDTO()
        val json = objectMapper.writeValueAsString(dto)

        Mockito.`when`(userService.getUserWithAuthorities()).thenReturn(null)

        mockMvc.perform(post("/api/query-builder/queryparticipant")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldGetAssignedQueriesBySubject() {
        mockMvc.perform(get("/api/query-builder/querygroups/subject/1"))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldDeleteAssignedQueryGroup() {
        mockMvc.perform(delete("/api/query-builder/querygroups/1/subject/2"))
            .andExpect(status().isOk)
    }
}
