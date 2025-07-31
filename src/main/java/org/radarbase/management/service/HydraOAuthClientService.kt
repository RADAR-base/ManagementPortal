package org.radarbase.management.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.radarbase.auth.hydra.HydraOAuthClientDTO
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.web.rest.errors.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Hydra-based implementation of OAuthClientService using Ory Hydra OAuth2 API.
 * This service handles OAuth client operations through Ory Hydra's admin API.
 */
@Service
class HydraOAuthClientService(
    @Autowired private val managementPortalProperties: ManagementPortalProperties
) : OAuthClientService {

    private val adminUrl: String = managementPortalProperties.authServer.serverAdminUrl
    private val publicUrl: String = managementPortalProperties.authServer.serverUrl

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = Duration.ofSeconds(30).toMillis()
            connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(30).toMillis()
        }
    }


    private class HydraClientDetails(
        private val hydraClient: HydraOAuthClientDTO
    ) : ClientDetails {
        override fun getClientId(): String = hydraClient.clientId
        override fun getResourceIds(): Set<String> = emptySet()
        override fun isSecretRequired(): Boolean = !hydraClient.clientSecret.isNullOrEmpty()
        override fun getClientSecret(): String? = hydraClient.clientSecret
        override fun isScoped(): Boolean = !hydraClient.scope.isNullOrEmpty()
        override fun getScope(): Set<String> = hydraClient.scope?.split(" ")?.toSet() ?: emptySet()
        override fun getAuthorizedGrantTypes(): Set<String> = hydraClient.grantTypes?.toSet() ?: emptySet()
        override fun getRegisteredRedirectUri(): Set<String> = hydraClient.redirectUris?.toSet() ?: emptySet()
        override fun getAuthorities(): Collection<org.springframework.security.core.GrantedAuthority> = emptyList()
        override fun getAccessTokenValiditySeconds(): Int? = null
        override fun getRefreshTokenValiditySeconds(): Int? = null
        override fun isAutoApprove(scope: String?): Boolean = false
        override fun getAdditionalInformation(): Map<String, Any> = mapOf(
            "client_name" to (hydraClient.clientName ?: ""),
            "owner" to (hydraClient.owner ?: ""),
            "created_at" to (hydraClient.createdAt ?: ""),
            "updated_at" to (hydraClient.updatedAt ?: "")
        )
    }

    override fun findAllOAuthClients(): List<ClientDetails> = runBlocking {
        withContext(Dispatchers.IO) {
            try {
                val response = httpClient.get {
                    url("$adminUrl/admin/clients")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }
                if (response.status.isSuccess()) {
                    val clients: List<HydraOAuthClientDTO> = response.body()
                    clients.map { HydraClientDetails(it) }
                } else {
                    log.error("Failed to fetch OAuth clients from Hydra: ${response.status}")
                    throw RuntimeException("Failed to fetch OAuth clients from Hydra")
                }
            } catch (e: Exception) {
                log.error("Error fetching OAuth clients from Hydra", e)
                throw RuntimeException("Error fetching OAuth clients from Hydra", e)
            }
        }
    }

    override fun findOneByClientId(clientId: String?): ClientDetails = runBlocking {
        withContext(Dispatchers.IO) {
            if (clientId.isNullOrEmpty()) {
                throw NotFoundException(
                    "Client ID cannot be null or empty", EntityName.Companion.OAUTH_CLIENT,
                    ErrorConstants.ERR_OAUTH_CLIENT_ID_NOT_FOUND, mapOf("clientId" to clientId)
                )
            }

            try {
                val response = httpClient.get {
                    url("$adminUrl/admin/clients/$clientId")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    val client: HydraOAuthClientDTO = response.body()
                    HydraClientDetails(client)
                } else if (response.status == HttpStatusCode.NotFound) {
                    log.error("OAuth client not found for client id: {}", clientId)
                    throw NotFoundException(
                        "Client not found for client id", EntityName.Companion.OAUTH_CLIENT,
                        ErrorConstants.ERR_OAUTH_CLIENT_ID_NOT_FOUND, mapOf("clientId" to clientId)
                    )
                } else {
                    log.error("Failed to fetch OAuth client from Hydra: ${response.status}")
                    throw RuntimeException("Failed to fetch OAuth client from Hydra")
                }
            } catch (e: NotFoundException) {
                throw e
            } catch (e: Exception) {
                log.error("Error fetching OAuth client from Hydra", e)
                throw RuntimeException("Error fetching OAuth client from Hydra", e)
            }
        }
    }

    override fun createClientDetail(clientDetailsDto: ClientDetailsDTO): ClientDetails = runBlocking {
        withContext(Dispatchers.IO) {
            val clientId = clientDetailsDto.clientId ?: throw IllegalArgumentException("Client ID cannot be null")
            val hydraClient = HydraOAuthClientDTO(
                clientId = clientId,
                clientName = clientId, // Use clientId as name if no name provided
                clientSecret = clientDetailsDto.clientSecret,
                redirectUris = clientDetailsDto.registeredRedirectUri?.toList(),
                grantTypes = clientDetailsDto.authorizedGrantTypes?.toList(),
                responseTypes = listOf("code"), // Default response type
                scope = clientDetailsDto.scope?.joinToString(" "),
                tokenEndpointAuthMethod = "client_secret_basic"
            )

            try {
                val response = httpClient.post {
                    url("$adminUrl/admin/clients")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(hydraClient)
                }

                if (response.status.isSuccess()) {
                    val createdClient: HydraOAuthClientDTO = response.body()
                    log.info("Created OAuth client with ID: {}", createdClient.clientId)
                    HydraClientDetails(createdClient)
                } else if (response.status == HttpStatusCode.Conflict) {
                    throw ConflictException(
                        "OAuth client already exists with this id",
                        EntityName.Companion.OAUTH_CLIENT, ErrorConstants.ERR_CLIENT_ID_EXISTS,
                        mapOf("client_id" to clientDetailsDto.clientId)
                    )
                } else {
                    log.error("Failed to create OAuth client in Hydra: ${response.status}")
                    throw RuntimeException("Failed to create OAuth client in Hydra")
                }
            } catch (e: ConflictException) {
                throw e
            } catch (e: Exception) {
                log.error("Error creating OAuth client in Hydra", e)
                throw RuntimeException("Error creating OAuth client in Hydra", e)
            }
        }
    }

    override fun updateOauthClient(clientDetailsDto: ClientDetailsDTO): ClientDetails = runBlocking {
        withContext(Dispatchers.IO) {
            val clientId = clientDetailsDto.clientId ?: throw IllegalArgumentException("Client ID cannot be null")
            val hydraClient = HydraOAuthClientDTO(
                clientId = clientId,
                clientName = clientId,
                clientSecret = clientDetailsDto.clientSecret,
                redirectUris = clientDetailsDto.registeredRedirectUri?.toList(),
                grantTypes = clientDetailsDto.authorizedGrantTypes?.toList(),
                responseTypes = listOf("code"),
                scope = clientDetailsDto.scope?.joinToString(" "),
                tokenEndpointAuthMethod = "client_secret_basic"
            )

            try {
                val response = httpClient.put {
                    url("$adminUrl/admin/clients/${clientDetailsDto.clientId}")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(hydraClient)
                }

                if (response.status.isSuccess()) {
                    val updatedClient: HydraOAuthClientDTO = response.body()
                    log.info("Updated OAuth client with ID: {}", updatedClient.clientId)
                    HydraClientDetails(updatedClient)
                } else if (response.status == HttpStatusCode.NotFound) {
                    throw NotFoundException(
                        "Client not found for client id", EntityName.Companion.OAUTH_CLIENT,
                        ErrorConstants.ERR_OAUTH_CLIENT_ID_NOT_FOUND, mapOf("clientId" to clientDetailsDto.clientId)
                    )
                } else {
                    log.error("Failed to update OAuth client in Hydra: ${response.status}")
                    throw RuntimeException("Failed to update OAuth client in Hydra")
                }
            } catch (e: NotFoundException) {
                throw e
            } catch (e: Exception) {
                log.error("Error updating OAuth client in Hydra", e)
                throw RuntimeException("Error updating OAuth client in Hydra", e)
            }
        }
    }

    override fun deleteClientDetails(clientId: String?) = runBlocking {
        withContext(Dispatchers.IO) {
            if (clientId.isNullOrEmpty()) {
                throw IllegalArgumentException("Client ID cannot be null or empty")
            }

            try {
                val response = httpClient.delete {
                    url("$adminUrl/admin/clients/$clientId")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    log.info("Deleted OAuth client with ID: {}", clientId)
                } else if (response.status == HttpStatusCode.NotFound) {
                    log.warn("OAuth client not found for deletion: {}", clientId)
                    // Don't throw exception for delete operations if client doesn't exist
                } else {
                    log.error("Failed to delete OAuth client from Hydra: ${response.status}")
                    throw RuntimeException("Failed to delete OAuth client from Hydra")
                }
            } catch (e: Exception) {
                log.error("Error deleting OAuth client from Hydra", e)
                throw RuntimeException("Error deleting OAuth client from Hydra", e)
            }
        }
    }

    override fun createAccessToken(user: User, clientId: String): OAuth2AccessToken {
        // For Hydra implementation, token creation would typically be handled by Hydra's token endpoint
        // This method might not be directly applicable in Hydra context as tokens are created through OAuth flows
        throw UnsupportedOperationException(
            "Direct token creation is not supported in Hydra implementation. " +
            "Use OAuth2 authorization flows through Hydra's token endpoint instead."
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(HydraOAuthClientService::class.java)
    }
}
