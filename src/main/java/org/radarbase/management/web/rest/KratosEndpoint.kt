package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import java.net.URISyntaxException
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.auth.kratos.SessionService
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.KratosSubjectWebhookDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kratos")
class KratosEndpoint
@Autowired
constructor(
        @Autowired private val subjectService: SubjectService,
        @Autowired private val subjectRepository: SubjectRepository,
        @Autowired private val projectService: ProjectService,
        @Autowired private val userService: UserService,
        @Autowired private val identityService: IdentityService,
        @Autowired private val managementPortalProperties: ManagementPortalProperties,
        @Autowired private val subjectMapper: SubjectMapper,
) {
    private var sessionService: SessionService =
            SessionService(managementPortalProperties.identityServer.publicUrl())

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
    ): ResponseEntity<Void> {
        val kratosIdentity =
                webhookDTO.identity ?: throw IllegalArgumentException("Identity is required")

        if (!kratosIdentity.schema_id.equals(KRATOS_SUBJECT_SCHEMA))
                throw IllegalArgumentException("Cannot create non-subject users")

        val id = kratosIdentity.id ?: throw IllegalArgumentException("Identity ID is required")
        val project =
                kratosIdentity.traits!!.projects?.firstOrNull()
                        ?: throw NotAuthorizedException("Cannot create subject without project")
        val projectUserId = project.userId ?: throw IllegalArgumentException("Project user ID is required")
        val projectDto =
                projectService.findOneByName(project.id!!)
                        ?: throw NotFoundException(
                                "Project not found: ${project.id!!}",
                                EntityName.PROJECT,
                                "projectNotFound"
                        )
        val subjectDto =
                subjectService.createSubject(projectUserId, projectDto)
                        ?: throw IllegalStateException("Failed to create subject for ID: $id")
        val user =
                userService.getUserWithAuthoritiesByLogin(subjectDto.login!!)
                        ?: throw NotFoundException(
                                "User not found with login: ${subjectDto.login}",
                                EntityName.USER,
                                "userNotFound"
                        )

        identityService.updateIdentityMetadataWithRoles(kratosIdentity, user)
        return ResponseEntity.created(ResourceUriService.getUri(subjectDto))
                .headers(HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, id))
                .build()
    }

    @PostMapping("/subjects/activate")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    suspend fun activateSubject(
            @RequestBody webhookDTO: KratosSubjectWebhookDTO,
    ): ResponseEntity<Void> {
        val id = webhookDTO.identity?.id ?: throw IllegalArgumentException("Subject ID is required")
        val token =
                webhookDTO.cookies?.get("ory_kratos_session")
                        ?: throw IllegalArgumentException("Session token is required")
        val kratosIdentity = sessionService.getSession(token).identity
        val project =
                kratosIdentity.traits!!.projects?.firstOrNull()
                ?: throw NotAuthorizedException("Cannot create subject without project")
        val projectUserId = project.userId ?: throw IllegalArgumentException("Project user ID is required")

        if (!hasPermission(kratosIdentity, id)) {
            throw NotAuthorizedException("Not authorized to activate subject")
        }
        subjectService.activateSubject(projectUserId)
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(EntityName.SUBJECT, id))
                .build()
    }

    private fun hasPermission(
            kratosIdentity: KratosSessionDTO.Identity,
            identityId: String?,
    ): Boolean = kratosIdentity.id == identityId

    companion object {
        private val logger = LoggerFactory.getLogger(KratosEndpoint::class.java)
        private val KRATOS_SUBJECT_SCHEMA = "subject"
    }
}