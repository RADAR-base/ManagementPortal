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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

/**
 * Service class for managing identities.
 */
@Service
@Transactional
class IdentityService(
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val authService: AuthService
) {
    private val httpClient = HttpClient(CIO).config {
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    lateinit var adminUrl: String
    lateinit var publicUrl: String

    init {
        adminUrl = managementPortalProperties.identityServer.adminUrl()
        publicUrl = managementPortalProperties.identityServer.publicUrl()

        log.debug("kratos serverUrl set to ${managementPortalProperties.identityServer.publicUrl()}")
        log.debug("kratos serverAdminUrl set to ${managementPortalProperties.identityServer.adminUrl()}")
    }

    /** Save a [User] to the IDP as an identity. Returns the generated [KratosSessionDTO.Identity] */
    @Throws(IdpException::class)
    suspend fun saveAsIdentity(user: User): KratosSessionDTO.Identity? {
        val kratosIdentity: KratosSessionDTO.Identity?

        withContext(Dispatchers.IO) {
            val identity = createIdentity(user)

            val postRequestBuilder = HttpRequestBuilder().apply {
                url("${adminUrl}/admin/identities")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(identity)
            }
            val response = httpClient.post(postRequestBuilder)

            if (response.status.isSuccess()) {
                kratosIdentity = response.body<KratosSessionDTO.Identity>()
                log.debug("saved identity for user ${user.login} to IDP as ${kratosIdentity.id}")
            } else {
                throw IdpException(
                    "couldn't save Kratos ID to server at " + adminUrl,
                )
            }
        }

        return kratosIdentity
    }

    /** Update a [User] as to the IDP as an identity. Returns the updated [KratosSessionDTO.Identity] */
    @Throws(IdpException::class)
    suspend fun updateAssociatedIdentity(user: User): KratosSessionDTO.Identity? {
        val kratosIdentity: KratosSessionDTO.Identity?

        user.identity ?: throw IdpException(
            "user ${user.login} could not be updated on the IDP. No identity was set",
        )

        withContext(Dispatchers.IO) {
            val identity = createIdentity(user)
            val response = httpClient.put {
                url("${adminUrl}/admin/identities/${user.identity}")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(identity)
            }


            if (response.status.isSuccess()) {
                kratosIdentity = response.body<KratosSessionDTO.Identity>()
                log.debug("Updated identity for user ${user.login} to IDP as ${kratosIdentity.id}")
            } else {
                throw IdpException(
                    "Couldn't update identity to server at $adminUrl"
                )
            }
        }

        return kratosIdentity
    }

    /** Delete a [User] as to the IDP as an identity. */
    @Throws(IdpException::class)
    suspend fun deleteAssociatedIdentity(userIdentity: String?) {
        withContext(Dispatchers.IO) {
            userIdentity ?: throw IdpException(
                "user with ID ${userIdentity} could not be deleted from the IDP. No identity was set"
            )

            val response = httpClient.delete {
                url("${adminUrl}/admin/identities/${userIdentity}")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }


            if (response.status.isSuccess()) {
                log.debug("Deleted identity for user ${userIdentity}")
            } else {
                throw IdpException(
                    "Couldn't delete identity from server at " + managementPortalProperties.identityServer.serverUrl
                )
            }
        }
    }

    /**
     * Convert a [User] to a [KratosSessionDTO.Identity] object.
     * @param user The object to convert
     * @return the newly created DTO object
     */
    @Throws(IdpException::class)
    private fun createIdentity(user: User): KratosSessionDTO.Identity {
        try {
            return KratosSessionDTO.Identity(
                schema_id = "user",
                traits = KratosSessionDTO.Traits(email = user.email),
                metadata_public = KratosSessionDTO.Metadata(
                    aud = emptyList(),
                    sources = emptyList(), //empty at the time of creation
                    roles = user.roles.mapNotNull { role: Role ->
                        val auth = role.authority?.name
                        when (role.role?.scope) {
                            RoleAuthority.Scope.GLOBAL -> auth
                            RoleAuthority.Scope.ORGANIZATION -> role.organization!!.name + ":" + auth
                            RoleAuthority.Scope.PROJECT -> role.project!!.projectName + ":" + auth
                            null -> null
                        }
                    }.toList(),
                    authorities = user.authorities,
                    scope = Permission.scopes().filter { scope ->
                        val permission = Permission.ofScope(scope)
                        val auths = user.roles.mapNotNull { it.role }

                        return@filter authService.mayBeGranted(auths, permission)
                    },
                    mp_login = user.login
                )
            )
        }
        catch (e: Throwable){
            val message = "could not convert user ${user.login} to identity"
            log.error(message)
            throw IdpException(message, e)
        }
    }

    /**
     * get a recovery link from the identityprovider in the response, which expires in 24 hours.
     * @param user The user for whom the recovery link is requested.
     * @return The recovery link obtained from the server response.
     * @throws IdpException If there is an issue with the identity or if the recovery link cannot be obtained from the server.
     */
    @Throws(IdpException::class)
    suspend fun getRecoveryLink(user: User): String {
        val recoveryLink: String

        user.identity ?: throw IdpException(
            "user ${user.login} could not be recovered on the IDP. No identity was set",
        )

        withContext(Dispatchers.IO) {
            val response = httpClient.post {
                url("${adminUrl}/admin/recovery/link")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "expires_in" to "24h",
                        "identity_id" to user.identity
                    )
                )
            }

            if (response.status.isSuccess()) {
                recoveryLink = response.body<Map<String, String>>()["recovery_link"]!!
                log.debug("recovery link for user ${user.login} is $recoveryLink")
            } else {
                throw IdpException(
                    "couldn't get recovery link from server at $adminUrl"
                )
            }
        }

        return recoveryLink
    }

    companion object {
        private val log = LoggerFactory.getLogger(IdentityService::class.java)
    }
}
