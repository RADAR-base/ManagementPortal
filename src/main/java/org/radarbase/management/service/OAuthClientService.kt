package org.radarbase.management.service

import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.radarbase.management.web.rest.errors.ConflictException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.springframework.stereotype.Service
import java.util.*

/**
 * The service layer to handle OAuthClient and Token related functions.
 * Created by nivethika on 03/08/2018.
 */
@Service
class OAuthClientService(
    @Autowired private val clientDetailsService: JdbcClientDetailsService,
    @Autowired private val clientDetailsMapper: ClientDetailsMapper,
    @Autowired private val authorizationServerEndpointsConfiguration: AuthorizationServerEndpointsConfiguration,
) {
    fun findAllOAuthClients(): List<ClientDetails> = clientDetailsService.listClientDetails()

    /**
     * Find ClientDetails by OAuth client id.
     *
     * @param clientId The client ID to look up
     * @return a ClientDetails object with the requested client ID
     * @throws NotFoundException If there is no client with the requested ID
     */
    fun findOneByClientId(clientId: String?): ClientDetails =
        try {
            clientDetailsService.loadClientByClientId(clientId)
        } catch (e: NoSuchClientException) {
            log.error("Pair client request for unknown client id: {}", clientId)
            val errorParams: MutableMap<String, String?> = HashMap()
            errorParams["clientId"] = clientId
            throw NotFoundException(
                "Client not found for client-id",
                EntityName.Companion.OAUTH_CLIENT,
                ErrorConstants.ERR_OAUTH_CLIENT_ID_NOT_FOUND,
                errorParams,
            )
        }

    /**
     * Update Oauth-client with new information.
     *
     * @param clientDetailsDto information to update.
     * @return Updated [ClientDetails] instance.
     */
    fun updateOauthClient(clientDetailsDto: ClientDetailsDTO): ClientDetails {
        val details: ClientDetails = clientDetailsMapper.clientDetailsDTOToClientDetails(clientDetailsDto)
        // update client.
        clientDetailsService.updateClientDetails(details)
        val updated = findOneByClientId(clientDetailsDto.clientId)
        // updateClientDetails does not update secret, so check for it separately
        if (clientDetailsDto.clientSecret != null && clientDetailsDto.clientSecret != updated.clientSecret) {
            clientDetailsService.updateClientSecret(
                clientDetailsDto.clientId,
                clientDetailsDto.clientSecret,
            )
        }
        return findOneByClientId(clientDetailsDto.clientId)
    }

    /**
     * Deletes an oauth client.
     * @param clientId of the auth-client to delete.
     */
    fun deleteClientDetails(clientId: String?) {
        clientDetailsService.removeClientDetails(clientId)
    }

    /**
     * Creates new oauth-client.
     *
     * @param clientDetailsDto data to create oauth-client.
     * @return created [ClientDetails].
     */
    fun createClientDetail(clientDetailsDto: ClientDetailsDTO): ClientDetails {
        // check if the client id exists
        try {
            val existingClient = clientDetailsService.loadClientByClientId(clientDetailsDto.clientId)
            if (existingClient != null) {
                throw ConflictException(
                    "OAuth client already exists with this id",
                    EntityName.Companion.OAUTH_CLIENT,
                    ErrorConstants.ERR_CLIENT_ID_EXISTS,
                    Collections.singletonMap<String, String?>("client_id", clientDetailsDto.clientId),
                )
            }
        } catch (ex: NoSuchClientException) {
            // Client does not exist yet, we can go ahead and create it
            log.info(
                "No client existing with client-id {}. Proceeding to create new client",
                clientDetailsDto.clientId,
            )
        }
        val details: ClientDetails = clientDetailsMapper.clientDetailsDTOToClientDetails(clientDetailsDto)
        // create oauth client.
        clientDetailsService.addClientDetails(details)
        return findOneByClientId(clientDetailsDto.clientId)
    }

    /**
     * Internally creates an [OAuth2AccessToken] token using authorization-code flow. This
     * method bypasses the usual authorization code flow mechanism, so it should only be used where
     * appropriate, e.g., for subject impersonation.
     *
     * @param clientId oauth client id.
     * @param user user of the token.
     * @return Created [OAuth2AccessToken] instance.
     */
    fun createAccessToken(
        user: User,
        clientId: String,
    ): OAuth2AccessToken {
        val authorities =
            user.authorities
                .map { a -> SimpleGrantedAuthority(a) }
        // lookup the OAuth client
        // getOAuthClient checks if the id exists
        val client = findOneByClientId(clientId)
        val requestParameters =
            Collections.singletonMap(
                OAuth2Utils.GRANT_TYPE,
                "authorization_code",
            )
        val responseTypes = setOf("code")
        val oAuth2Request =
            OAuth2Request(
                requestParameters,
                clientId,
                authorities,
                true,
                client.scope,
                client.resourceIds,
                null,
                responseTypes,
                emptyMap(),
            )
        val authenticationToken: Authentication =
            UsernamePasswordAuthenticationToken(
                user.login,
                null,
                authorities,
            )
        return authorizationServerEndpointsConfiguration
            .getEndpointsConfigurer()
            .tokenServices
            .createAccessToken(OAuth2Authentication(oAuth2Request, authenticationToken))
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuthClientService::class.java)
        private const val PROTECTED_KEY = "protected"

        /**
         * Checks whether a client is a protected client.
         *
         * @param details ClientDetails.
         */
        fun checkProtected(details: ClientDetails) {
            val info = details.additionalInformation
            if (Objects.nonNull(info) &&
                info.containsKey(PROTECTED_KEY) &&
                info[PROTECTED_KEY]
                    .toString()
                    .equals("true", ignoreCase = true)
            ) {
                throw InvalidRequestException(
                    "Cannot modify protected client",
                    EntityName.Companion.OAUTH_CLIENT,
                    ErrorConstants.ERR_OAUTH_CLIENT_PROTECTED,
                    Collections.singletonMap<String, String?>("client_id", details.clientId),
                )
            }
        }
    }
}
