package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.MetaTokenService
import org.radarbase.management.service.OAuthClientService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.dto.ClientPairInfoDTO
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.MalformedURLException
import java.net.URISyntaxException
import javax.validation.Valid

/**
 * Created by dverbeec on 5/09/2017.
 */
@RestController
@RequestMapping("/api")
class OAuthClientsResource(
    @Autowired private val oAuthClientService: OAuthClientService,
    @Autowired private val metaTokenService: MetaTokenService,
    @Autowired private val clientDetailsMapper: ClientDetailsMapper,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val userService: UserService,
    @Autowired private val eventRepository: AuditEventRepository,
    @Autowired private val authService: AuthService
) {

    @Throws(NotAuthorizedException::class)
    @Timed
    @GetMapping("/oauth-clients")
    /**
     * GET /api/oauth-clients.
     *
     *
     * Retrieve a list of currently registered OAuth clients.
     *
     * @return the list of registered clients as a list of [ClientDetailsDTO]
     */
    fun oAuthClients(): ResponseEntity<List<ClientDetailsDTO>> {
        authService.checkScope(Permission.OAUTHCLIENTS_READ)
        val clients = clientDetailsMapper.clientDetailsToClientDetailsDTO(oAuthClientService.findAllOAuthClients())
        return ResponseEntity.ok().body(clients)
    }

    /**
     * GET /api/oauth-clients/:id.
     *
     *
     * Get details on a specific client.
     *
     * @param id the client id for which to fetch the details
     * @return the client as a [ClientDetailsDTO]
     */
    @GetMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getOAuthClientById(@PathVariable("id") id: String?): ResponseEntity<ClientDetailsDTO> {
        authService.checkPermission(Permission.OAUTHCLIENTS_READ)

        val client = oAuthClientService.findOneByClientId(id)
        val clientDTO = clientDetailsMapper.clientDetailsToClientDetailsDTO(client)

        // getOAuthClient checks if the id exists
        return ResponseEntity.ok().body(clientDTO)
    }

    /**
     * PUT /api/oauth-clients.
     *
     *
     * Update an existing OAuth client.
     *
     * @param clientDetailsDto The client details to update
     * @return The updated OAuth client.
     */
    @PutMapping("/oauth-clients")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun updateOAuthClient(@RequestBody @Valid clientDetailsDto: ClientDetailsDTO?): ResponseEntity<ClientDetailsDTO> {
        authService.checkPermission(Permission.OAUTHCLIENTS_UPDATE)
        // getOAuthClient checks if the id exists
        OAuthClientService.checkProtected(oAuthClientService.findOneByClientId(clientDetailsDto!!.clientId))
        val updated = oAuthClientService.updateOauthClient(clientDetailsDto)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    EntityName.OAUTH_CLIENT,
                    clientDetailsDto.clientId
                )
            )
            .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(updated))
    }

    /**
     * DELETE /api/oauth-clients/:id.
     *
     *
     * Delete the OAuth client with the specified client id.
     *
     * @param id The id of the client to delete
     * @return a ResponseEntity indicating success or failure
     */
    @DeleteMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteOAuthClient(@PathVariable id: String?): ResponseEntity<Void> {
        authService.checkPermission(Permission.OAUTHCLIENTS_DELETE)
        // getOAuthClient checks if the id exists
        OAuthClientService.checkProtected(oAuthClientService.findOneByClientId(id))
        oAuthClientService.deleteClientDetails(id)
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(EntityName.OAUTH_CLIENT, id))
            .build()
    }

    /**
     * POST /api/oauth-clients.
     *
     *
     * Register a new oauth client.
     *
     * @param clientDetailsDto The OAuth client to be registered
     * @return a response indicating success or failure
     * @throws URISyntaxException if there was a problem formatting the URI to the new entity
     */
    @PostMapping("/oauth-clients")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createOAuthClient(@RequestBody clientDetailsDto: @Valid ClientDetailsDTO): ResponseEntity<ClientDetailsDTO> {
        authService.checkPermission(Permission.OAUTHCLIENTS_CREATE)
        val created = oAuthClientService.createClientDetail(clientDetailsDto)
        return ResponseEntity.created(ResourceUriService.getUri(clientDetailsDto))
            .headers(HeaderUtil.createEntityCreationAlert(EntityName.OAUTH_CLIENT, created.clientId))
            .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(created))
    }

    /**
     * GET /oauth-clients/pair.
     *
     *
     * Generates OAuth2 refresh tokens for the given user, to be used to bootstrap the
     * authentication of client apps. This will generate a refresh token which can be used at the
     * /oauth/token endpoint to get a new access token and refresh token.
     *
     * @param login the login of the subject for whom to generate pairing information
     * @param clientId the OAuth client id
     * @return a ClientPairInfoDTO with status 200 (OK)
     */
    @GetMapping("/oauth-clients/pair")
    @Timed
    @Throws(NotAuthorizedException::class, URISyntaxException::class, MalformedURLException::class)
    fun getRefreshToken(
        @RequestParam login: String,
        @RequestParam(value = "clientId") clientId: String,
        @RequestParam(value = "persistent", defaultValue = "false") persistent: Boolean?
    ): ResponseEntity<ClientPairInfoDTO> {
        authService.checkScope(Permission.SUBJECT_UPDATE)
        val currentUser =
            userService.userWithAuthorities // We only allow this for actual logged in users for now, not for client_credentials
                ?: throw AccessDeniedException(
                        "You must be a logged in user to access this resource"
                    )

        // lookup the subject
        val subject = subjectService.findOneByLogin(login)
        val projectName: String = subject.activeProject
            ?.projectName
            ?: throw NotFoundException(
                    "Project for subject $login not found", EntityName.SUBJECT,
                    ErrorConstants.ERR_SUBJECT_NOT_FOUND
                )


        // Users who can update a subject can also generate a refresh token for that subject
        authService.checkPermission(
            Permission.SUBJECT_UPDATE,
            { e: EntityDetails -> e.project(projectName).subject(login) })
        val cpi = metaTokenService.createMetaToken(subject, clientId, persistent!!)
        // generate audit event
        eventRepository.add(
            AuditEvent(
                currentUser.login, "PAIR_CLIENT_REQUEST",
                "client_id=$clientId", "subject_login=$login"
            )
        )
        log.info(
            "[{}] by {}: client_id={}, subject_login={}", "PAIR_CLIENT_REQUEST", currentUser
                .login, clientId, login
        )
        return ResponseEntity(cpi, HttpStatus.OK)
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuthClientsResource::class.java)
    }
}
