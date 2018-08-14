package org.radarcns.management.service;

import static org.radarcns.management.service.dto.ProjectDTO.PRIVACY_POLICY_URL;
import static org.radarcns.management.web.rest.MetaTokenResource.DEFAULT_META_TOKEN_TIMEOUT;
import static org.radarcns.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
import org.radarcns.management.service.mapper.ClientDetailsMapper;
import org.radarcns.management.web.rest.errors.ConflictException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.radarcns.management.web.rest.errors.InvalidRequestException;
import org.radarcns.management.web.rest.errors.InvalidStateException;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.stereotype.Service;

/**
 * The service layer to handle OAuthClient and Token related functions.
 * Created by nivethika on 03/08/2018.
 */
@Service
public class OAuthClientService {

    private final Logger log = LoggerFactory.getLogger(OAuthClientService.class);

    private static final String PROTECTED_KEY = "protected";

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ClientDetailsMapper clientDetailsMapper;

    @Autowired
    private MetaTokenService metaTokenService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration;

    public List<ClientDetails> findAllOAuthClients() {
        return clientDetailsService.listClientDetails();
    }

    /**
     * Find ClientDetails by OAuth client id.
     *
     * @param clientId The client ID to look up
     * @return a ClientDetails object with the requested client ID
     * @throws NotFoundException If there is no client with the requested ID
     */
    public ClientDetails findOneByClientId(String clientId) {
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

    /**
     * Update Oauth-client with new information.
     *
     * @param clientDetailsDto information to update.
     * @return Updated {@link ClientDetails} instance.
     */
    public ClientDetails updateOauthClient(ClientDetailsDTO clientDetailsDto) {
        ClientDetails details =
                clientDetailsMapper.clientDetailsDTOToClientDetails(clientDetailsDto);
        // update client.
        clientDetailsService.updateClientDetails(details);

        ClientDetails updated = findOneByClientId(clientDetailsDto.getClientId());
        // updateClientDetails does not update secret, so check for it separately
        if (clientDetailsDto.getClientSecret() != null && !clientDetailsDto.getClientSecret()
                .equals(updated.getClientSecret())) {
            clientDetailsService.updateClientSecret(clientDetailsDto.getClientId(),
                    clientDetailsDto.getClientSecret());
        }
        return findOneByClientId(clientDetailsDto.getClientId());
    }

    /**
     * Checks whether a client is a protected client.
     *
     * @param details ClientDetails.
     */
    public static void checkProtected(ClientDetails details) {
        Map<String, Object> info = details.getAdditionalInformation();
        if (Objects.nonNull(info) && info.containsKey(PROTECTED_KEY) && info.get(PROTECTED_KEY)
                .toString().equalsIgnoreCase("true")) {
            throw new InvalidRequestException("Cannot modify protected client", OAUTH_CLIENT,
                ErrorConstants.ERR_OAUTH_CLIENT_PROTECTED,
                Collections.singletonMap("client_id", details.getClientId()));
        }
    }

    /**
     * Deletes an oauth client.
     * @param clientId of the auth-client to delete.
     */
    public void deleteClientDetails(String clientId) {
        clientDetailsService.removeClientDetails(clientId);
    }

    /**
     * Creates new oauth-client.
     *
     * @param clientDetailsDto data to create oauth-client.
     * @return created {@link ClientDetails}.
     */
    public ClientDetails createClientDetail(ClientDetailsDTO clientDetailsDto) {
        // check if the client id exists
        try {
            ClientDetails existingClient =
                    clientDetailsService.loadClientByClientId(clientDetailsDto.getClientId());
            if (existingClient != null) {
                throw new ConflictException("OAuth client already exists with this id",
                    OAUTH_CLIENT, ErrorConstants.ERR_CLIENT_ID_EXISTS,
                    Collections.singletonMap("client_id", clientDetailsDto.getClientId()));
            }
        } catch (NoSuchClientException ex) {
            // Client does not exist yet, we can go ahead and create it
            log.info("No client existing with client-id {}. Proceeding to create new client",
                    clientDetailsDto.getClientId());
        }
        ClientDetails details =
                clientDetailsMapper.clientDetailsDTOToClientDetails(clientDetailsDto);
        // create oauth client.
        clientDetailsService.addClientDetails(details);

        return findOneByClientId(clientDetailsDto.getClientId());

    }

    /**
     * Creates refresh token for oauth-subject pair.
     * @param subject to create token for
     * @param clientId using which client id
     * @return {@link ClientPairInfoDTO} to return.
     * @throws URISyntaxException when token URI cannot be formed properly.
     * @throws MalformedURLException when token URL cannot be formed properly.
     */
    public ClientPairInfoDTO createRefreshToken(Subject subject, String clientId)
            throws URISyntaxException, MalformedURLException {

        // add the user's authorities
        User user = subject.getUser();
        Set<GrantedAuthority> authorities =
                user.getAuthorities().stream().map(a -> new SimpleGrantedAuthority(a.getName()))
                .collect(Collectors.toSet());
        // lookup the OAuth client
        // getOAuthClient checks if the id exists
        ClientDetails details = findOneByClientId(clientId);

        OAuth2AccessToken token =
                createAuthorizationCodeToken(clientId, user.getLogin(), authorities,
                    details.getScope(), details.getResourceIds());
        // tokenName should be generated
        MetaToken metaToken = metaTokenService
                .saveUniqueToken(subject, token.getRefreshToken().getValue(), false,
                Instant.now().plus(getMetaTokenTimeout()));

        if (metaToken.getId() != null && metaToken.getTokenName() != null) {
            // get base url from settings
            String baseUrl = managementPortalProperties.getCommon().getBaseUrl();
            // create complete uri string
            String tokenUrl = baseUrl + ResourceUriService.getUri(metaToken).getPath();
            // create response
            return new ClientPairInfoDTO(metaToken.getTokenName(), new URL(tokenUrl));
        } else {
            throw new InvalidStateException("Could not create a valid token", OAUTH_CLIENT,
                "error.couldNotCreateToken");
        }
    }




    /**
     * Gets the meta-token timeout from config file. If the config is not mentioned or in wrong
     * format, it will return default value.
     *
     * @return meta-token timeout duration.
     */
    private Duration getMetaTokenTimeout() {

        String timeoutConfig = managementPortalProperties.getOauth().getMetaTokenTimeout();

        if (timeoutConfig.isEmpty()) {
            return DEFAULT_META_TOKEN_TIMEOUT;
        }

        try {
            return Duration.parse(managementPortalProperties.getOauth().getMetaTokenTimeout());
        } catch (DateTimeParseException e) {
            // if the token timeout cannot be read, log the error and use the default value.
            log.warn("Cannot parse meta-token timeout config. Using default value", e);
            return DEFAULT_META_TOKEN_TIMEOUT;
        }
    }

    /**
     * Creates the actual {@link OAuth2AccessToken} token using authorization-code flow.
     *
     * @param clientId    oauth client id.
     * @param login       subject-id of the token.
     * @param authorities authorities to create token.
     * @param scope       scopes of the token.
     * @param resourceIds resource-ids of the token.
     * @return Created {@link OAuth2AccessToken} instance.
     */
    private OAuth2AccessToken createAuthorizationCodeToken(String clientId, String login,
            Set<GrantedAuthority> authorities, Set<String> scope, Set<String> resourceIds) {

        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put(GRANT_TYPE , "authorization_code");


        Set<String> responseTypes = Collections.singleton("code");

        OAuth2Request oAuth2Request =
                new OAuth2Request(requestParameters, clientId, authorities, true, scope,
                    resourceIds, null, responseTypes, Collections.emptyMap());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login, null, authorities);
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

        AuthorizationServerTokenServices tokenServices =
                authorizationServerEndpointsConfiguration.getEndpointsConfigurer()
                    .getTokenServices();

        return tokenServices.createAccessToken(auth);
    }
}
