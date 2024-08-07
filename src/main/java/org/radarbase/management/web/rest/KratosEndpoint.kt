package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.kratos.SessionService
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.IdentityService
import org.radarbase.management.service.ProjectService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.dto.KratosSubjectWebhookDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URISyntaxException
import java.util.*

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
        suspend fun createSubject(
            @RequestBody webhookDTO: KratosSubjectWebhookDTO,
        ): ResponseEntity<SubjectDTO> {
            val kratosSession = sessionService.getSession(webhookDTO.session_token!!)
            val kratosIdentity = kratosSession.identity
            
            if (kratosIdentity.id == webhookDTO.identity_id) {
                val projectName = webhookDTO.project_id
                val projectDto = projectService.findOneByName(projectName!!)
                val subjectDto = SubjectDTO()
                subjectDto.login = webhookDTO.identity_id
                subjectDto.project = projectDto

                if (!subjectDto.externalId.isNullOrEmpty() &&
                    subjectRepository.findOneByProjectNameAndExternalId(
                        projectName,
                        subjectDto.externalId,
                    ) != null
                ) {
                    throw BadRequestException(
                        "A subject with given project-id and" + "external-id already exists",
                        EntityName.SUBJECT,
                        "subjectExists",
                    )
                }
                val resultDto = subjectService.createSubject(subjectDto)
                val subject = subjectService.findOneByLogin(resultDto!!.login)
                kratosIdentity.metadata_public = identityService.createIdentityMetadata(subject.user!!)
                identityService.updateAssociatedIdentity(kratosIdentity)
                return ResponseEntity
                    .created(ResourceUriService.getUri(subjectDto))
                    .headers(
                        HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, subject.user!!.login),
                    ).body(resultDto)
            } else {
                throw NotAuthorizedException("Not authorized to create subject")
            }
        }

        companion object {
            private val logger = LoggerFactory.getLogger(KratosEndpoint::class.java)
        }
    }
