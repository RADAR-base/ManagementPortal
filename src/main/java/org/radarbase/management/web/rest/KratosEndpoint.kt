package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.ProjectService
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.web.rest.util.HeaderUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import tech.jhipster.web.util.ResponseUtil
import java.net.URISyntaxException
import java.util.*
import javax.validation.Valid
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.KratosSubjectWebhookDTO
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.service.ResourceUriService

@RestController
@RequestMapping("/api/kratos")
class KratosEndpoint
    @Autowired
    constructor(
        @Autowired private val subjectService: SubjectService,
        @Autowired private val subjectRepository: SubjectRepository,
        @Autowired private val projectService: ProjectService,    
    ) {
        /**
         * POST  /subjects : Create a new subject.
         *
         * @param subjectDto the subjectDto to create
         * @return the ResponseEntity with status 201 (Created) and with body the new subjectDto, or
         * with status 400 (Bad Request) if the subject has already an ID
         * @throws URISyntaxException if the Location URI syntax is incorrect
         */
        @PostMapping("/subjects")
        @Timed
        @Throws(URISyntaxException::class, NotAuthorizedException::class)
        fun createSubject(@RequestBody webhookDTO: KratosSubjectWebhookDTO): ResponseEntity<SubjectDTO> {
            val projectName = webhookDTO.project_id
            val projectDto = projectService.findOneByName(projectName!!) 
            // TODO: Add permission
            // authService.checkPermission(Permission.SUBJECT_CREATE, { e: EntityDetails -> e.project(projectName) })
            val subjectDto = SubjectDTO()
            subjectDto.externalId = webhookDTO.identity_id
            subjectDto.project = projectDto

            if (!subjectDto.externalId.isNullOrEmpty()
                && subjectRepository.findOneByProjectNameAndExternalId(
                    projectName, subjectDto.externalId
                ) != null
            ) {
                throw BadRequestException(
                    "A subject with given project-id and"
                            + "external-id already exists", EntityName.SUBJECT, "subjectExists"
                )
            }
            val result = subjectService.createSubject(subjectDto)
            return ResponseEntity.created(ResourceUriService.getUri(subjectDto))
                .headers(HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, result?.login))
                .body(result)
        }

        companion object {
            private val logger = LoggerFactory.getLogger(KratosEndpoint::class.java)
        }
    }
