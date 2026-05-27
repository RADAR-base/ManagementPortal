package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.auth.kratos.SessionService
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.KratosSubjectWebhookDTO
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URISyntaxException
import org.radarbase.management.config.annotations.IdentityServerDisabled

@IdentityServerDisabled
@RestController
@RequestMapping("/api/webhook/kratos")
private class WebhookResource
@Autowired
constructor(
        @Autowired private val subjectService: SubjectService,
        @Autowired private val subjectRepository: SubjectRepository,
        @Autowired private val projectService: ProjectService,
        @Autowired private val userService: UserService,
        @Autowired private val managementPortalProperties: ManagementPortalProperties,
) {
    private var sessionService: SessionService =
            SessionService(managementPortalProperties.identityServer.publicUrl)

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
        logger.debug("REST request to create subject : $webhookDTO")
        val kratosIdentity =
                webhookDTO.identity ?: throw IllegalArgumentException("Identity is required")
        val id = kratosIdentity.id ?: throw IllegalArgumentException("Identity ID is required")

        // Verify kratos identity exists
        // val existingIdentity = identityService.getExistingIdentity(id)
        // if (!existingIdentity.equals(kratosIdentity))
        //         throw IllegalArgumentException("Kratos identity does not match")

        if (!kratosIdentity.schema_id.equals(KRATOS_SUBJECT_SCHEMA))
                throw IllegalArgumentException("Cannot create non-subject users")

        val project =
                kratosIdentity.traits?.projects?.firstOrNull()
                        ?: throw NotAuthorizedException("Cannot create subject without project")
        val projectUserId =
                project.userId ?: throw IllegalArgumentException("Project user ID is required")
        val projectDto =
                projectService.findOneByName(project.id!!)
                        ?: throw NotFoundException(
                                "Project not found: ${project.id!!}",
                                EntityName.PROJECT,
                                "projectNotFound"
                        )
        val email = kratosIdentity.traits?.email.orEmpty()
        val subjectDto =
                subjectService.createSubject(
                        projectUserId,
                        projectDto,
                        id,
                        mapOf(EMAIL_ATTRIBUTE_KEY to email)
                )
                        ?: throw IllegalStateException("Failed to create subject for ID: $id")
        
        userService.getUserWithAuthoritiesByLogin(login = subjectDto.login!!)?.let { user ->
                try {
                        userService.updateUser(user.login!!, user.firstName, user.lastName, user.email, user.langKey)
                } catch (ex: Exception) {
                        logger.error("Failed to update associated identity for user {}: {}", user.login, ex.message)
                }
        }
                        

        return ResponseEntity.created(ResourceUriService.getUri(subjectDto))
                .headers(HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, id))
                .build()
    }

    @PostMapping("/subjects/activate")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    suspend fun activateUser(
            @RequestBody webhookDTO: KratosSubjectWebhookDTO,
    ): ResponseEntity<Void> {
        val identityId = webhookDTO.identity?.id 
                ?: throw IllegalArgumentException("Identity ID is required")
        val sessionToken = webhookDTO.cookies?.get("ory_kratos_session")
                ?: throw IllegalArgumentException("Session token is required")
        
        val kratosIdentity = sessionService.getSession(sessionToken).identity
        val schemaId = kratosIdentity.schema_id 
                ?: throw IllegalArgumentException("Schema ID is required")

        validatePermission(kratosIdentity, identityId)

        return when (schemaId) {
            KRATOS_SUBJECT_SCHEMA -> activateSubjectIdentity(webhookDTO, kratosIdentity, identityId)
            KRATOS_RESEARCHER_SCHEMA -> activateResearcherIdentity(kratosIdentity, identityId)
            else -> throw IllegalArgumentException("Unsupported schema type: $schemaId")
        }
    }

    private suspend fun activateSubjectIdentity(
            webhookDTO: KratosSubjectWebhookDTO,
            kratosIdentity: KratosSessionDTO.Identity,
            identityId: String
    ): ResponseEntity<Void> {
        val project = kratosIdentity.traits?.projects?.firstOrNull()
                ?: throw NotAuthorizedException("Cannot create subject without project")
        val projectUserId = project.userId 
                ?: throw IllegalArgumentException("Project user ID is required")
        
        // Ensure subject exists before activating
        try {
            subjectService.findOneByLogin(projectUserId)
        } catch (e: NotFoundException) {
            createSubject(webhookDTO)
        }
        subjectService.activateSubject(projectUserId)
        
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(EntityName.SUBJECT, identityId))
                .build()
    }

    private suspend fun activateResearcherIdentity(
            kratosIdentity: KratosSessionDTO.Identity,
            identityId: String
    ): ResponseEntity<Void> {
        val mpLogin = kratosIdentity.metadata_public?.mp_login
                ?: throw IllegalArgumentException("Management Portal login is required for researcher activation")
        
        val userDto = userService.getUserDtoWithAuthoritiesByLogin(mpLogin)
                ?: throw NotFoundException(
                        "User not found: $mpLogin",
                        EntityName.USER,
                        "userNotFound"
                )
        
        if (!userDto.isActivated) {
            userDto.isActivated = true
            userService.updateUser(userDto)
        }
        
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(EntityName.USER, identityId))
                .build()
    }

    private fun validatePermission(kratosIdentity: KratosSessionDTO.Identity, identityId: String) {
        if (kratosIdentity.id != identityId) {
            throw NotAuthorizedException("Not authorized to activate user")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebhookResource::class.java)
        private val KRATOS_SUBJECT_SCHEMA = "subject"
        private val KRATOS_RESEARCHER_SCHEMA = "researcher"
        private val EMAIL_ATTRIBUTE_KEY = "email"
    }
}
