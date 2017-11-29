package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.ClientDetailsMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_CREATE;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_DELETE;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_READ;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_UPDATE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarcns.management.security.SecurityUtils.getJWT;
/**
 * Created by dverbeec on 5/09/2017.
 */
@RestController
@RequestMapping("/api")
public class OAuthClientsResource {

    private final Logger log = LoggerFactory.getLogger(OAuthClientsResource.class);

    @Autowired
    private AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration;

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ClientDetailsMapper clientDetailsMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private AuditEventRepository eventRepository;

    private static final String ENTITY_NAME = "oauthClient";
    private static final String PROTECTED_KEY = "protected";

    /**
     * GET /api/oauth-clients
     *
     * Retrieve a list of currently registered OAuth clients.
     *
     * @return the list of registered clients as a list of {@link ClientDetailsDTO}
     */
    @GetMapping("/oauth-clients")
    @Timed
    public ResponseEntity<List<ClientDetailsDTO>> getOAuthClients() {
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_READ);
        return ResponseEntity.ok().body(clientDetailsMapper
                .clientDetailsToClientDetailsDTO(clientDetailsService.listClientDetails()));
    }

    /**
     * GET /api/oauth-clients/:id
     *
     * Get details on a specific client.
     *
     * @param id the client id for which to fetch the details
     * @return the client as a {@link ClientDetailsDTO}
     */
    @GetMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<ClientDetailsDTO> getOAuthClientById(@PathVariable("id") String id) {
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_READ);
        // getOAuthClient checks if the id exists
        return ResponseEntity.ok().body(clientDetailsMapper
                .clientDetailsToClientDetailsDTO(getOAuthClient(id)));
    }

    /**
     * PUT /api/oauth-clients
     *
     * Update an existing OAuth client.
     *
     * @param clientDetailsDTO The client details to update
     * @return The updated OAuth client.
     */
    @PutMapping("/oauth-clients")
    @Timed
    public ResponseEntity<ClientDetailsDTO> updateOAuthClient(@RequestBody ClientDetailsDTO
            clientDetailsDTO) {
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_UPDATE);
        // check if we have an ID field supplied
        checkClientFields(clientDetailsDTO);
        // getOAuthClient checks if the id exists
        checkProtected(getOAuthClient(clientDetailsDTO.getClientId()));
        ClientDetails details = clientDetailsMapper
                .clientDetailsDTOToClientDetails(clientDetailsDTO);
        clientDetailsService.updateClientDetails(details);
        ClientDetails updated = getOAuthClient(clientDetailsDTO.getClientId());
        // updateClientDetails does not update secret, so check for it separately
        if (Objects.nonNull(clientDetailsDTO.getClientSecret()) && !clientDetailsDTO
                .getClientSecret().equals(updated.getClientSecret())) {
            clientDetailsService.updateClientSecret(clientDetailsDTO.getClientId(),
                    clientDetailsDTO.getClientSecret());
        }
        updated = getOAuthClient(clientDetailsDTO.getClientId());
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME,
                        clientDetailsDTO.getClientId()))
                .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(updated));
    }

    /**
     * DELETE /api/oauth-clients/:id
     *
     * Delete the OAuth client with the specified client id.
     *
     * @param id The id of the client to delete
     * @return a ResponseEntity indicating success or failure
     */
    @DeleteMapping("/oauth-clients/{id:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteOAuthClient(@PathVariable String id) {
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_DELETE);
        // getOAuthClient checks if the id exists
        checkProtected(getOAuthClient(id));
        clientDetailsService.removeClientDetails(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id))
                .build();
    }

    /**
     * POST /api/oauth-clients
     *
     * Register a new oauth client
     *
     * @param clientDetailsDTO The oauth client to be registered
     * @return a response indicating success or failure
     * @throws URISyntaxException if there was a problem formatting the URI to the new entity
     */
    @PostMapping("/oauth-clients")
    @Timed
    public ResponseEntity<ClientDetailsDTO> createOAuthClient(@RequestBody ClientDetailsDTO
            clientDetailsDTO) throws URISyntaxException {
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_CREATE);
        // check if we have an ID field supplied
        checkClientFields(clientDetailsDTO);
        // check if the client id exists
        try {
            ClientDetails existing = clientDetailsService.loadClientByClientId(clientDetailsDTO
                    .getClientId());
            throw new CustomConflictException("An OAuth client with that ID already exists",
                    Collections.singletonMap("client_id", clientDetailsDTO.getClientId()));
        } catch (NoSuchClientException ex) {
            // Client does not exist yet, we can go ahead and create it
        }
        ClientDetails details = clientDetailsMapper
                .clientDetailsDTOToClientDetails(clientDetailsDTO);
        clientDetailsService.addClientDetails(details);
        ClientDetails created = getOAuthClient(clientDetailsDTO.getClientId());
        return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "oauth-clients",
                created.getClientId())))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, created.getClientId()))
                .body(clientDetailsMapper.clientDetailsToClientDetailsDTO(created));
    }

    /**
     * GET /oauth-clients/pair
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
    public ResponseEntity<ClientPairInfoDTO> getRefreshToken(@RequestParam String login,
            @RequestParam(value="clientId") String clientId) {
        User currentUser = userService.getUserWithAuthorities();
        if (currentUser == null) {
            // We only allow this for actual logged in users for now, not for client_credentials
            throw new AccessDeniedException("You must be a logged in user to access this resource");
        }

        // lookup the subject
        Subject subject = getSubject(login);
        SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(subject);

        // Users who can update a subject can also generate a refresh token for that subject
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
            subjectDTO.getProject().getProjectName(), subjectDTO.getLogin());

        // lookup the OAuth client
        // getOAuthClient checks if the id exists
        ClientDetails details = getOAuthClient(clientId);

        // add the user's authorities
        User user = subject.getUser();
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getAuthorities().stream()
            .forEach(a -> authorities.add(new SimpleGrantedAuthority(a.getName())));

        OAuth2AccessToken token = createToken(clientId, user.getLogin(), authorities,
            details.getScope(), details.getResourceIds());

        ClientPairInfoDTO cpi = new ClientPairInfoDTO(token.getRefreshToken().getValue());

        // generate audit event
        eventRepository.add(new AuditEvent(currentUser.getLogin(),"PAIR_CLIENT_REQUEST",
                "client_id=" + clientId, "subject_login=" + login));
        log.info("[{}] by {}: client_id={}, subject_login={}", "PAIR_CLIENT_REQUEST", currentUser
                .getLogin(), clientId, login);
        return new ResponseEntity<>(cpi, HttpStatus.OK);
    }

    /**
     * Check a supplied ClientDetailsDTO for necessary fields. ClientID is the only required one.
     *
     * @param client The ClientDetailsDTO to check
     * @throws CustomParameterizedException if the client ID is null or empty.
     */
    private void checkClientFields(ClientDetailsDTO client) throws CustomParameterizedException {
        try {
            Objects.requireNonNull(client.getClientId(), "Client ID can not be null");
        } catch (NullPointerException ex) {
            throw new CustomParameterizedException(ex.getMessage());
        }
        if (client.getClientId().equals("")) {
            throw new CustomParameterizedException("Client ID can not be empty");
        }
    }

    private OAuth2AccessToken createToken(String clientId, String login,
        Set<GrantedAuthority> authorities, Set<String> scope, Set<String> resourceIds) {
        Map<String, String> requestParameters = new HashMap<>();

        boolean approved = true;

        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        Map<String, Serializable> extensionProperties = new HashMap<>();

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, authorities,
            approved, scope, resourceIds, null, responseTypes, extensionProperties);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(login, null, authorities);
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

        AuthorizationServerTokenServices tokenServices =
            authorizationServerEndpointsConfiguration.getEndpointsConfigurer().getTokenServices();

        return tokenServices.createAccessToken(auth);
    }

    /**
     * Find ClientDetails by OAuth client id.
     * @param clientId The client ID to look up
     * @return a ClientDetails object with the requested client ID
     * @throws CustomNotFoundException If there is no client with the requested ID
     */
    private ClientDetails getOAuthClient(String clientId) throws CustomNotFoundException {
        try {
            return clientDetailsService.loadClientByClientId(clientId);
        }
        catch (NoSuchClientException e) {
            log.error("Pair client request for unknown client id: {}", clientId);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Client ID not found");
            errorParams.put("clientId", clientId);
            throw new CustomNotFoundException("Client ID not found", errorParams);
        }
    }

    private Subject getSubject(String login) throws CustomNotFoundException {
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);

        if (!subject.isPresent()) {
            log.error("Pair client request for unknown subject login: {}", login);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Subject ID not found");
            errorParams.put("subjectLogin", login);
            throw new CustomNotFoundException("Subject ID not found", errorParams);
        }

        return subject.get();
    }

    private void checkProtected(ClientDetails details) {
        Map<String, Object> info = details.getAdditionalInformation();
        if (Objects.nonNull(info) && info.containsKey(PROTECTED_KEY)
                && info.get(PROTECTED_KEY).toString().equalsIgnoreCase("true")) {
            throw new CustomParameterizedException("Modification of a protected OAuth client is "
                    + "not allowed.");
        }
    }
}
