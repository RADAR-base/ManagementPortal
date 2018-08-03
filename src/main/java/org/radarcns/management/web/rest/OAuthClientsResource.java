package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.config.Constants;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.service.MetaTokenService;
import org.radarcns.management.service.ResourceUriService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.ClientDetailsMapper;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.ConflictException;
import org.radarcns.management.web.rest.errors.InvalidRequestException;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
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
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_CREATE;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_DELETE;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_READ;
import static org.radarcns.auth.authorization.Permission.OAUTHCLIENTS_UPDATE;
import static org.radarcns.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarcns.management.security.SecurityUtils.getJWT;
import static org.radarcns.management.web.rest.MetaTokenResource.DEFAULT_META_TOKEN_TIMEOUT;
import static org.radarcns.management.web.rest.errors.EntityName.OAUTH_CLIENT;

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

    @Autowired
    private MetaTokenService metaTokenService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    private static final String ENTITY_NAME = "oauthClient";
    private static final String PROTECTED_KEY = "protected";

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
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_READ);
        return ResponseEntity.ok().body(clientDetailsMapper
                .clientDetailsToClientDetailsDTO(clientDetailsService.listClientDetails()));
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
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_READ);
        // getOAuthClient checks if the id exists
        return ResponseEntity.ok().body(clientDetailsMapper
                .clientDetailsToClientDetailsDTO(getOAuthClient(id)));
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
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_UPDATE);
        // getOAuthClient checks if the id exists
        checkProtected(getOAuthClient(clientDetailsDto.getClientId()));
        ClientDetails details = clientDetailsMapper
                .clientDetailsDTOToClientDetails(clientDetailsDto);
        clientDetailsService.updateClientDetails(details);
        ClientDetails updated = getOAuthClient(clientDetailsDto.getClientId());
        // updateClientDetails does not update secret, so check for it separately
        if (Objects.nonNull(clientDetailsDto.getClientSecret()) && !clientDetailsDto
                .getClientSecret().equals(updated.getClientSecret())) {
            clientDetailsService.updateClientSecret(clientDetailsDto.getClientId(),
                    clientDetailsDto.getClientSecret());
        }
        updated = getOAuthClient(clientDetailsDto.getClientId());
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME,
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
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_DELETE);
        // getOAuthClient checks if the id exists
        checkProtected(getOAuthClient(id));
        clientDetailsService.removeClientDetails(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id))
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
        checkPermission(getJWT(servletRequest), OAUTHCLIENTS_CREATE);
        // check if the client id exists
        try {
            clientDetailsService.loadClientByClientId(clientDetailsDto.getClientId());
            throw new ConflictException("OAuth client already exists with this id", OAUTH_CLIENT,
                ErrorConstants.ERR_CLIENT_ID_EXISTS,
                    Collections.singletonMap("client_id", clientDetailsDto.getClientId()));
        } catch (NoSuchClientException ex) {
            // Client does not exist yet, we can go ahead and create it
        }
        ClientDetails details = clientDetailsMapper
                .clientDetailsDTOToClientDetails(clientDetailsDto);
        clientDetailsService.addClientDetails(details);
        ClientDetails created = getOAuthClient(clientDetailsDto.getClientId());
        return ResponseEntity.created(ResourceUriService.getUri(clientDetailsDto))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, created.getClientId()))
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
            @RequestParam(value = "clientId") String clientId)
            throws NotAuthorizedException, URISyntaxException {
        User currentUser = userService.getUserWithAuthorities();
        if (currentUser == null) {
            // We only allow this for actual logged in users for now, not for client_credentials
            throw new AccessDeniedException("You must be a logged in user to access this resource");
        }

        // lookup the subject
        Subject subject = getSubject(login);
        SubjectDTO subjectDto = subjectMapper.subjectToSubjectDTO(subject);

        // Users who can update a subject can also generate a refresh token for that subject
        checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                subjectDto.getProject().getProjectName(), subjectDto.getLogin());

        // lookup the OAuth client
        // getOAuthClient checks if the id exists
        ClientDetails details = getOAuthClient(clientId);

        // add the user's authorities
        User user = subject.getUser();
        Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(a -> new SimpleGrantedAuthority(a.getName()))
                .collect(Collectors.toSet());

        OAuth2AccessToken token = createToken(clientId, user.getLogin(), authorities,
                details.getScope(), details.getResourceIds());
        // tokenName should be generated
        MetaToken metaToken = metaTokenService.saveUniqueToken(token.getRefreshToken().getValue(),
                false, Instant.now().plus(getMetaTokenTimeout()));

        ClientPairInfoDTO cpi = null;
        if (metaToken.getId() != null && metaToken.getTokenName() != null) {
            // get base url from settings
            String baseUrl = managementPortalProperties.getCommon().getBaseUrl();
            // create complete uri string
            String url = baseUrl + ResourceUriService.getUri(metaToken).getPath();
            // create response
            cpi = new ClientPairInfoDTO(metaToken.getTokenName(), new URI(url));
        }
        // generate audit event
        eventRepository.add(new AuditEvent(currentUser.getLogin(), "PAIR_CLIENT_REQUEST",
                "client_id=" + clientId, "subject_login=" + login));
        log.info("[{}] by {}: client_id={}, subject_login={}", "PAIR_CLIENT_REQUEST", currentUser
                .getLogin(), clientId, login);
        return new ResponseEntity<>(cpi, HttpStatus.OK);
    }

    /**
     * Gets the meta-token timeout from config file. If the config is not mentioned or in wrong
     * format, it will return default value.
     * @return meta-token timeout duration.
     */
    private Duration getMetaTokenTimeout() {

        String timeoutConfig = managementPortalProperties.getOauth().getMetaTokenTimeout();

        if (timeoutConfig.isEmpty()) {
            return DEFAULT_META_TOKEN_TIMEOUT;
        }

        try {
            return Duration.parse(managementPortalProperties.getOauth()
                .getMetaTokenTimeout());
        } catch (DateTimeParseException e) {
            // if the token timeout cannot be read, log the error and use the default value.
            log.warn("Cannot parse meta-token timeout config. Using default value" , e);
            return DEFAULT_META_TOKEN_TIMEOUT;
        }
    }

    private OAuth2AccessToken createToken(String clientId, String login,
            Set<GrantedAuthority> authorities, Set<String> scope, Set<String> resourceIds) {
        Map<String, String> requestParameters = new HashMap<>();

        Set<String> responseTypes = Collections.singleton("code");

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, authorities,
                true, scope, resourceIds, null, responseTypes, Collections.emptyMap());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login, null, authorities);
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

        AuthorizationServerTokenServices tokenServices = authorizationServerEndpointsConfiguration
                .getEndpointsConfigurer().getTokenServices();

        return tokenServices.createAccessToken(auth);
    }

    /**
     * Find ClientDetails by OAuth client id.
     *
     * @param clientId The client ID to look up
     * @return a ClientDetails object with the requested client ID
     * @throws NotFoundException If there is no client with the requested ID
     */
    private ClientDetails getOAuthClient(String clientId) throws NotFoundException {
        try {
            return clientDetailsService.loadClientByClientId(clientId);
        } catch (NoSuchClientException e) {
            log.error("Pair client request for unknown client id: {}", clientId);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("clientId", clientId);
            throw new NotFoundException("Client not found for client-id", OAUTH_CLIENT,
                ErrorConstants.ERR_OAUTH_CLIENT_ID_NOT_FOUND, errorParams);
        }
    }

    private Subject getSubject(String login) throws NotFoundException {
        Optional<Subject> subject = subjectRepository.findOneWithEagerBySubjectLogin(login);

        if (!subject.isPresent()) {
            log.error("Pair client request for unknown subject login: {}", login);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("subjectLogin", login);
            throw new NotFoundException("Subject not found for subjectLogin", OAUTH_CLIENT,
                ErrorConstants
                .ERR_SUBJECT_NOT_FOUND, errorParams);
        }

        return subject.get();
    }

    private void checkProtected(ClientDetails details) {
        Map<String, Object> info = details.getAdditionalInformation();
        if (Objects.nonNull(info) && info.containsKey(PROTECTED_KEY)
                && info.get(PROTECTED_KEY).toString().equalsIgnoreCase("true")) {
            throw new InvalidRequestException("Cannot modify protected client",
                OAUTH_CLIENT, ErrorConstants.ERR_OAUTH_CLIENT_PROTECTED,
                Collections.singletonMap("client_id", details.getClientId()));
        }
    }
}
