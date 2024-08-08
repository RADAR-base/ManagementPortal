package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.kratos.SessionService
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.KratosSubjectWebhookDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URISyntaxException

@RestController
@RequestMapping("/api/kratos")
class KratosEndpoint
    @Autowired
    constructor(
        @Autowired private val subjectService: SubjectService,
        @Autowired private val subjectRepository: SubjectRepository,
        @Autowired private val projectService: ProjectService,
        @Autowired private val authService: AuthService,
        @Autowired private val identityService: IdentityService,
        @Autowired private val managementPortalProperties: ManagementPortalProperties,
        @Autowired private val subjectMapper: SubjectMapper,
    ) {
        private var sessionService: SessionService = SessionService(managementPortalProperties.identityServer.publicUrl())

        /**
         * POST /subjects : Create a new subject.
         *
         * @param subjectDto the subjectDto to create
         * @return the ResponseEntity with status 201 (Created) and with body the new subjectDto, or
         * with status 400 (Bad Request) if the subject has already an ID
         * @throws URISyntaxException if the Location URI syntax is incorrect
         */
        @PostMapping("/subjects")
        @Timed
        @Throws(URISyntaxException::class, NotAuthorizedException::class)
        suspend fun createSubject(@RequestBody webhookDTO: KratosSubjectWebhookDTO): ResponseEntity<SubjectDTO> {
            val id = webhookDTO.identity_id!!
            val projectId = webhookDTO.project_id!!
            if (projectId == null) {
                throw NotAuthorizedException("Cannot create subject without project") 
            }
            val token = webhookDTO.session_token!!
            val kratosIdentity = sessionService.getSession(token).identity

            if (!hasPermission(kratosIdentity, id)) {
                throw NotAuthorizedException("Not authorized to create subject")
            }

            val projectDto = projectService.findOneByName(projectId)
            val subjectDto = subjectService.createSubject(id, projectDto)
            val user = subjectMapper.subjectDTOToSubject(subjectDto)?.user
            val identityDto = identityService.updateKratosIdentityMetadata(kratosIdentity, user!!)

            return ResponseEntity
                .created(ResourceUriService.getUri(subjectDto!!))
                .headers(HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, id))
                .body(subjectDto)
        }

        private fun hasPermission(kratosIdentity: KratosSessionDTO.Identity, identityId: String?): Boolean {
            return kratosIdentity.id == identityId
        }

        companion object {
            private val logger = LoggerFactory.getLogger(KratosEndpoint::class.java)
        }
    }
