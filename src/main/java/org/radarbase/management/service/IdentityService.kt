package org.radarbase.management.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Service class for managing identities. */
@Service
@Transactional
class IdentityService
@Autowired
constructor(
        private val managementPortalProperties: ManagementPortalProperties,
        private val authService: AuthService
) {
    private val httpClient =
            HttpClient(CIO) {
                install(HttpTimeout) {
                    connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
                }
                install(ContentNegotiation) {
                    json(
                            Json {
                                ignoreUnknownKeys = true
                                coerceInputValues = true
                            }
                    )
                }
            }

    private val adminUrl = managementPortalProperties.identityServer.adminUrl()
    private val publicUrl = managementPortalProperties.identityServer.publicUrl()

    init {
        log.debug("Kratos serverUrl set to $publicUrl")
        log.debug("Kratos serverAdminUrl set to $adminUrl")
    }

    /**
     * Convert a [User] to a [KratosSessionDTO.Identity] object.
     * @param user The object to convert
     * @return the newly created DTO object
     */
    @Throws(IdpException::class)
    private fun createIdentity(user: User): KratosSessionDTO.Identity =
            try {
                KratosSessionDTO.Identity(
                        schema_id = "researcher",
                        traits = KratosSessionDTO.Traits(email = user.email),
                        metadata_public =
                                KratosSessionDTO.Metadata(
                                        aud = emptyList(),
                                        sources = emptyList(),
                                        roles =
                                                user.roles.mapNotNull { role ->
                                                    role.authority?.name?.let { auth ->
                                                        when (role.role?.scope) {
                                                            RoleAuthority.Scope.GLOBAL -> auth
                                                            RoleAuthority.Scope.ORGANIZATION ->
                                                                    "${role.organization!!.name}:$auth"
                                                            RoleAuthority.Scope.PROJECT ->
                                                                    "${role.project!!.projectName}:$auth"
                                                            null -> null
                                                        }
                                                    }
                                                },
                                        authorities = user.authorities,
                                        scope =
                                                Permission.scopes().filter { scope ->
                                                    authService.mayBeGranted(
                                                            user.roles.mapNotNull { it.role },
                                                            Permission.ofScope(scope)
                                                    )
                                                },
                                        mp_login = user.login
                                )
                )
            } catch (e: Throwable) {
                val message = "Could not convert user ${user.login} to identity"
                log.error(message)
                throw IdpException(message, e)
            }

    /**
     * Save a [User] to the IDP as an identity. Returns the generated [KratosSessionDTO.Identity]
     */
    @Throws(IdpException::class)
    suspend fun saveAsIdentity(user: User): KratosSessionDTO.Identity =
            withContext(Dispatchers.IO) {
                val identity = createIdentity(user)
                val response =
                        httpClient.post {
                            url("$adminUrl/admin/identities")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                            setBody(identity)
                        }

                if (response.status.isSuccess()) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Saved identity for user ${user.login} to IDP as ${it.id}")
                    }
                } else {
                    throw IdpException("Couldn't save Kratos ID to server at $adminUrl")
                }
            }

    /**
     * Update a [User] to the IDP as an identity. Returns the updated [KratosSessionDTO.Identity]
     */
    @Throws(IdpException::class)
    suspend fun updateAssociatedIdentity(user: User): KratosSessionDTO.Identity =
            withContext(Dispatchers.IO) {
                val identityId =
                        user.identity
                                ?: throw IdpException(
                                        "User ${user.login} could not be updated on the IDP. No identity was set"
                                )

                val identity = createIdentity(user)
                val response =
                        httpClient.put {
                            url("$adminUrl/admin/identities/$identityId")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                            setBody(identity)
                        }

                if (response.status.isSuccess()) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Updated identity for user ${user.login} on IDP as ${it.id}")
                    }
                } else {
                    throw IdpException("Couldn't update identity on server at $adminUrl")
                }
            }

    /**
     * Update [KratosSessionDTO.Identity] metadata with user roles. Returns the updated
     * [KratosSessionDTO.Identity]
     */
    @Throws(IdpException::class)
    suspend fun updateIdentityMetadataWithRoles(
            identity: KratosSessionDTO.Identity,
            user: UserDTO
    ): KratosSessionDTO.Identity =
            withContext(Dispatchers.IO) {
                val updatedIdentity = identity.copy(metadata_public = getIdentityMetadata(user))

                val response =
                        httpClient.put {
                            url("$adminUrl/admin/identities/${updatedIdentity.id}")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                            setBody(updatedIdentity)
                        }

                if (response.status.isSuccess()) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Updated identity for ${it.id}")
                    }
                } else {
                    throw IdpException("Couldn't update identity on server at $adminUrl")
                }
            }

    /** Delete a [User] from the IDP as an identity. */
    @Throws(IdpException::class)
    suspend fun deleteAssociatedIdentity(userIdentity: String?) =
            withContext(Dispatchers.IO) {
                val identityId =
                        userIdentity
                                ?: throw IdpException(
                                        "User with ID $userIdentity could not be deleted from the IDP. No identity was set"
                                )

                val response =
                        httpClient.delete {
                            url("$adminUrl/admin/identities/$identityId")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                        }

                if (response.status.isSuccess()) {
                    log.debug("Deleted identity for user $identityId")
                } else {
                    throw IdpException("Couldn't delete identity from server at $adminUrl")
                }
            }

    /**
     * Convert a [UserDTO] to a [KratosSessionDTO.Metadata] object.
     * @param user The object to convert
     * @return the newly created DTO object
     */
    @Throws(IdpException::class)
    fun getIdentityMetadata(user: UserDTO): KratosSessionDTO.Metadata =
            try {
                KratosSessionDTO.Metadata(
                        aud = emptyList(),
                        sources = emptyList(),
                        roles =
                                user.roles.orEmpty().mapNotNull { role ->
                                    role.authorityName?.let { auth ->
                                        when {
                                            role.projectName != null -> "${role.projectName}:$auth"
                                            role.organizationName != null ->
                                                    "${role.organizationName}:$auth"
                                            else -> auth
                                        }
                                    }
                                },
                        authorities = user.authorities.orEmpty(),
                        scope =
                                Permission.scopes().filter { scope ->
                                    authService.mayBeGranted(
                                            user.roles?.mapNotNull {
                                                RoleAuthority.valueOfAuthority(it.authorityName!!)
                                            }
                                                    ?: emptyList(),
                                            Permission.ofScope(scope)
                                    )
                                },
                        mp_login = user.login
                )
            } catch (e: Throwable) {
                val message = "Could not convert user ${user.login} to identity"
                log.error(message)
                throw IdpException(message, e)
            }

    /**
     * Get a recovery link from the identity provider, which expires in 24 hours.
     * @param user The user for whom the recovery link is requested.
     * @return The recovery link obtained from the server response.
     * @throws IdpException If there is an issue with the identity or if the recovery link cannot be
     * obtained.
     */
    @Throws(IdpException::class)
    suspend fun getRecoveryLink(user: User): String =
            withContext(Dispatchers.IO) {
                val identityId =
                        user.identity
                                ?: throw IdpException(
                                        "User ${user.login} could not be recovered on the IDP. No identity was set"
                                )

                val response =
                        httpClient.post {
                            url("$adminUrl/admin/recovery/link")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                            setBody(mapOf("expires_in" to "24h", "identity_id" to identityId))
                        }

                if (response.status.isSuccess()) {
                    response.body<Map<String, String>>()["recovery_link"]!!.also {
                        log.debug("Recovery link for user ${user.login} is $it")
                    }
                } else {
                    throw IdpException("Couldn't get recovery link from server at $adminUrl")
                }
            }

    companion object {
        private val log = LoggerFactory.getLogger(IdentityService::class.java)
    }
}
