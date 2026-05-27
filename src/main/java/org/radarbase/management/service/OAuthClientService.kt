package org.radarbase.management.service

import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetails

/**
 * Interface for managing OAuth clients.
 * This interface defines the contract for OAuth client management operations that can be implemented
 * by different OAuth client service implementations, including external OAuth services.
 */
interface OAuthClientService {

    /**
     * Find all OAuth clients.
     * @return list of all client details
     */
    fun findAllOAuthClients(): List<ClientDetails>

    /**
     * Find ClientDetails by OAuth client id.
     * @param clientId The client ID to look up
     * @return a ClientDetails object with the requested client ID
     * @throws NotFoundException If there is no client with the requested ID
     */
    fun findOneByClientId(clientId: String?): ClientDetails

    /**
     * Update Oauth-client with new information.
     * @param clientDetailsDto information to update.
     * @return Updated [ClientDetails] instance.
     */
    fun updateOauthClient(clientDetailsDto: ClientDetailsDTO): ClientDetails

    /**
     * Deletes an oauth client.
     * @param clientId of the auth-client to delete.
     */
    fun deleteClientDetails(clientId: String?)

    /**
     * Creates new oauth-client.
     * @param clientDetailsDto data to create oauth-client.
     * @return created [ClientDetails].
     */
    fun createClientDetail(clientDetailsDto: ClientDetailsDTO): ClientDetails

    /**
     * Internally creates an [OAuth2AccessToken] token using authorization-code flow. This
     * method bypasses the usual authorization code flow mechanism, so it should only be used where
     * appropriate, e.g., for subject impersonation.
     * @param user user of the token.
     * @param clientId oauth client id.
     * @return Created [OAuth2AccessToken] instance.
     */
    fun createAccessToken(user: User, clientId: String): OAuth2AccessToken
}
