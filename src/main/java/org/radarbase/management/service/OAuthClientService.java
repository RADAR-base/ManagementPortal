package org.radarbase.management.service;

import org.radarbase.management.domain.User;
import org.radarbase.management.service.dto.ClientDetailsDTO;
import org.radarbase.management.service.mapper.ClientDetailsMapper;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.InvalidRequestException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.radarbase.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;

/**
 * The service layer to handle OAuthClient and Token related functions.
 * Created by nivethika on 03/08/2018.
 */
@Service
public class OAuthClientService {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientService.class);

    private static final String PROTECTED_KEY = "protected";

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ClientDetailsMapper clientDetailsMapper;

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
     * Internally creates an {@link OAuth2AccessToken} token using authorization-code flow. This
     * method bypasses the usual authorization code flow mechanism, so it should only be used where
     * appropriate, e.g., for subject impersonation.
     *
     * @param clientId oauth client id.
     * @param user user of the token.
     * @return Created {@link OAuth2AccessToken} instance.
     */
    public OAuth2AccessToken createAccessToken(User user, String clientId) {
        Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(a -> new SimpleGrantedAuthority(a.getName()))
                .collect(Collectors.toSet());
        // lookup the OAuth client
        // getOAuthClient checks if the id exists
        ClientDetails client = findOneByClientId(clientId);

        Map<String, String> requestParameters = Collections.singletonMap(
                GRANT_TYPE , "authorization_code");

        Set<String> responseTypes = Collections.singleton("code");

        OAuth2Request oAuth2Request = new OAuth2Request(
                requestParameters, clientId, authorities, true, client.getScope(),
                client.getResourceIds(), null, responseTypes, Collections.emptyMap());

        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getLogin(), null, authorities);

        return authorizationServerEndpointsConfiguration.getEndpointsConfigurer()
                .getTokenServices()
                .createAccessToken(new OAuth2Authentication(oAuth2Request, authenticationToken));
    }
}
