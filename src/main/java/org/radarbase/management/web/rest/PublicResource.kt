package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.management.service.AWSService
import org.radarbase.management.service.DataSource
import org.radarbase.management.service.DataSummaryResult
import org.radarbase.management.service.ProjectService
import org.radarbase.management.web.rest.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public")
class PublicResource(
    @Autowired private val projectService: ProjectService
) {

    /**
     * GET  /public/projects : get all the projects for public endpoint.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of PublicProjectDTO
     */
    @GetMapping("projects")
    @Timed
    fun getProjectsInfo(
        @PageableDefault(size = Int.MAX_VALUE) pageable: Pageable,
    ): ResponseEntity<*> {
        log.debug("REST request to get Projects for public endpoint")
        val page = projectService.getPublicProjects(pageable)
        val headers = PaginationUtil
            .generatePaginationHttpHeaders(page, "/api/public/projects")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    companion object {
        private val log = LoggerFactory.getLogger(PublicResource::class.java)
    }


}
