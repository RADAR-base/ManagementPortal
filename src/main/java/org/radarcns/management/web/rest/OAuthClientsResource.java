package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
import org.radarcns.management.service.mapper.ClientDetailsMapper;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private UserService userService;

    @GetMapping("/oauthclients")
    @Timed
    public ResponseEntity<List<ClientDetailsDTO>> getOAuthClients() {
        return ResponseEntity.ok().body(clientDetailsMapper.clientDetailsToClientDetailsDTO(
            clientDetailsService.listClientDetails()));
    }

    @GetMapping("/oauthclients/:id")
    @Timed
    public ResponseEntity<ClientDetailsDTO> getOAuthClientById(@PathVariable("id") String id) {
        return ResponseEntity.ok().body(
            clientDetailsMapper.clientDetailsToClientDetailsDTO(getOAuthClient(id)));
    }

    /**
     * GET /oauthclients/pair
     *
     * Generates OAuth2 refresh tokens for the given user, to be used to bootstrap the
     * authentication of client apps. This will generate a refresh token which can be used at the
     * /oauth/token endpoint to get a new access token and refresh token.
     *
     * @param login the login of the subject for whom to generate pairing information
     * @param clientId the OAuth client id
     * @return a ClientPairInfoDTO with status 200 (OK)
     */
    @GetMapping("/oauthclients/pair")
    @Timed
    @Secured({ AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<ClientPairInfoDTO> getRefreshToken(@RequestParam String login,
        @RequestParam(value="clientId") String clientId) {

        User currentUser = userService.getUserWithAuthorities();
        if (currentUser == null) {
            // We only allow this for actual logged in users for now, not for client_credentials
            throw new AccessDeniedException("You must be a logged in user to access this resource");
        }

        // check if current user has project_admin role in the project the requested user is in
        // TODO finish this when roles implementation is complete
        /*
        if (!currentUser.getAuthorities().contains(AuthoritiesConstants.SYS_ADMIN) && currentUser.getRoles().stream()
            .filter(role -> role.getProject().getProjectName().equals(user.getProject().getProjectName()))
            .filter(role -> role.getAuthority().getName().equals(AuthoritiesConstants.PROJECT_ADMIN))
            .collect(Collectors.toList()).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }*/

        // lookup the OAuth client
        ClientDetails details = getOAuthClient(clientId);

        // lookup the subject
        Subject subject = getSubject(login);
        log.info("Pair client request for subject login: {}", login);

        // add the user's authorities
        User user = subject.getUser();
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getAuthorities().stream()
            .forEach(a -> authorities.add(new SimpleGrantedAuthority(a.getName())));

        log.info("Requesting token with scopes: [" + String.join(",", details.getScope()) + "] and "
            + " resource ids: [" + String.join(",", details.getResourceIds()) + "]");
        OAuth2AccessToken token = createToken(clientId, user.getLogin(), authorities,
            details.getScope(), details.getResourceIds());

        ClientPairInfoDTO cpi = new ClientPairInfoDTO(token.getRefreshToken().getValue());

        return new ResponseEntity<>(cpi, HttpStatus.OK);
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

    private ClientDetails getOAuthClient(String clientId) throws CustomNotFoundException {
        try {
            return clientDetailsService.loadClientByClientId(clientId);
        }
        catch (NoSuchClientException e) {
            log.info("Pair client request for unknown client id: {}", clientId);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Client ID not found");
            errorParams.put("clientId", clientId);
            throw new CustomNotFoundException("Client ID not found", errorParams);
        }
    }

    private Subject getSubject(String login) throws CustomNotFoundException {
        Subject subject = subjectRepository.findBySubjectLogin(login);

        if (subject == null) {
            log.info("Pair client request for unknown subject login: {}", login);
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Subject ID not found");
            errorParams.put("subjectLogin", login);
            throw new CustomNotFoundException("Subject ID not found", errorParams);
        }

        return subject;
    }
}
