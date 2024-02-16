package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.radarbase.management.security.Constants;
import org.radarbase.management.security.NotAuthorizedException;
import org.radarbase.management.service.AuthService;
import org.radarbase.management.service.MetaTokenService;
import org.radarbase.management.service.OAuthClientService;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.SubjectService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.ClientDetailsDTO;
import org.radarbase.management.service.dto.ClientPairInfoDTO;
import org.radarbase.management.service.mapper.ClientDetailsMapper;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.OAUTHCLIENTS_CREATE;
import static org.radarbase.auth.authorization.Permission.OAUTHCLIENTS_DELETE;
import static org.radarbase.auth.authorization.Permission.OAUTHCLIENTS_READ;
import static org.radarbase.auth.authorization.Permission.OAUTHCLIENTS_UPDATE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarbase.management.service.OAuthClientService.checkProtected;
import static org.radarbase.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SUBJECT_NOT_FOUND;

/**
 * Created by dverbeec on 5/09/2017.
 */
@RestController
@RequestMapping("/api")
public class OAuthClientsResource {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientsResource.class);


    @Autowired
    private OAuthClientService oAuthClientService;

    @Autowired
    private MetaTokenService metaTokenService;

    @Autowired
    private ClientDetailsMapper clientDetailsMapper;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditEventRepository eventRepository;

    @Autowired
    private AuthService authService;

    /**
     * GET /api/oauth-clients.
     *
     * <p>Retrieve a list of currently registered OAuth clients.</p>
     *
     * @return the list of registered clients as a list of {@link ClientDetailsDTO}
     */
    @GetMapping("/oauth-clients")
    @Timed
    public ResponseEntity<List<ClientDetailsDTO>> getOAuthClients() throws NotAuthorizedException {
        authService.checkScope(OAUTHCLIENTS_READ);

         var clients = clientDetailsMapper.clientDetailsToClientDetailsDTO(oAuthClientService.findAllOAuthClients());
        return ResponseEntity.ok().body(clients);

    }

    /**
     * GET /api/oauth-clients/:id.
     *
     * <p>Get details on a specific client.</p>
     *
     * @param id the client id for which to fetch the details
     * @return the client as a {@link ClientDetailsDTO}
     */
    @GetMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<ClientDetailsDTO> getOAuthClientById(@PathVariable("id") String id)
            throws NotAuthorizedException {
        authService.checkPermission(OAUTHCLIENTS_READ);
        // getOAuthClient checks if the id exists
        return ResponseEntity.ok().body(clientDetailsMapper
                .clientDetailsToClientDetailsDTO(oAuthClientService.findOneByClientId(id)));
    }

    /**
     * PUT /api/oauth-clients.
     *
     * <p>Update an existing OAuth client.</p>
     *
     * @param clientDetailsDto The client details to update
     * @return The updated OAuth client.
     */
    @PutMapping("/oauth-clients")
    @Timed
    public ResponseEntity<ClientDetailsDTO> updateOAuthClient(@Valid @RequestBody ClientDetailsDTO
            clientDetailsDto) throws NotAuthorizedException {
        authService.checkPermission(OAUTHCLIENTS_UPDATE);
        // getOAuthClient checks if the id exists
        checkProtected(oAuthClientService.findOneByClientId(clientDetailsDto.getClientId()));

        ClientDetails updated = oAuthClientService.updateOauthClient(clientDetailsDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(OAUTH_CLIENT,
                        clientDetailsDto.getClientId()))
                .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(updated));
    }

    /**
     * DELETE /api/oauth-clients/:id.
     *
     * <p>Delete the OAuth client with the specified client id.</p>
     *
     * @param id The id of the client to delete
     * @return a ResponseEntity indicating success or failure
     */
    @DeleteMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteOAuthClient(@PathVariable String id)
            throws NotAuthorizedException {
        authService.checkPermission(OAUTHCLIENTS_DELETE);
        // getOAuthClient checks if the id exists
        checkProtected(oAuthClientService.findOneByClientId(id));
        oAuthClientService.deleteClientDetails(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(OAUTH_CLIENT, id))
                .build();
    }

    /**
     * POST /api/oauth-clients.
     *
     * <p>Register a new oauth client.</p>
     *
     * @param clientDetailsDto The OAuth client to be registered
     * @return a response indicating success or failure
     * @throws URISyntaxException if there was a problem formatting the URI to the new entity
     */
    @PostMapping("/oauth-clients")
    @Timed
    public ResponseEntity<ClientDetailsDTO> createOAuthClient(@Valid @RequestBody ClientDetailsDTO
            clientDetailsDto) throws URISyntaxException, NotAuthorizedException {
        authService.checkPermission(OAUTHCLIENTS_CREATE);
        ClientDetails created = oAuthClientService.createClientDetail(clientDetailsDto);
        return ResponseEntity.created(ResourceUriService.getUri(clientDetailsDto))
                .headers(HeaderUtil.createEntityCreationAlert(OAUTH_CLIENT, created.getClientId()))
                .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(created));
    }

    /**
     * GET /oauth-clients/pair.
     *
     * <p>Generates OAuth2 refresh tokens for the given user, to be used to bootstrap the
     * authentication of client apps. This will generate a refresh token which can be used at the
     * /oauth/token endpoint to get a new access token and refresh token.</p>
     *
     * @param login the login of the subject for whom to generate pairing information
     * @param clientId the OAuth client id
     * @return a ClientPairInfoDTO with status 200 (OK)
     */
    @GetMapping("/oauth-clients/pair")
    @Timed
    public ResponseEntity<ClientPairInfoDTO> getRefreshToken(@RequestParam String login,
            @RequestParam(value = "clientId") String clientId,
            @RequestParam(value = "persistent", defaultValue = "false") Boolean persistent)
            throws NotAuthorizedException, URISyntaxException, MalformedURLException {
        authService.checkScope(SUBJECT_UPDATE);
        User currentUser = userService.getUserWithAuthorities()
                // We only allow this for actual logged in users for now, not for client_credentials
                .orElseThrow(() -> new AccessDeniedException(
                        "You must be a logged in user to access this resource"));

        // lookup the subject
        Subject subject = subjectService.findOneByLogin(login);
        String project = subject.getActiveProject()
                .map(Project::getProjectName)
                .orElseThrow(() -> new NotFoundException(
                        "Project for subject " + login + " not found", SUBJECT,
                        ERR_SUBJECT_NOT_FOUND));

        // Users who can update a subject can also generate a refresh token for that subject
        authService.checkPermission(SUBJECT_UPDATE, e -> e.project(project).subject(login));

        ClientPairInfoDTO cpi = metaTokenService.createMetaToken(subject, clientId, persistent);
        // generate audit event
        eventRepository.add(new AuditEvent(currentUser.getLogin(), "PAIR_CLIENT_REQUEST",
                "client_id=" + clientId, "subject_login=" + login));
        log.info("[{}] by {}: client_id={}, subject_login={}", "PAIR_CLIENT_REQUEST", currentUser
                .getLogin(), clientId, login);
        return new ResponseEntity<>(cpi, HttpStatus.OK);
    }

}
