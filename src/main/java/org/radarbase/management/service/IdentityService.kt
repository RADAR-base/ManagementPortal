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
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.kratos.KratosSessionDTO
import org.radarbase.auth.kratos.KratosSessionDTO.JsonMetadataPatchOperation
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.Subject
import org.radarbase.management.domain.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

/** Service class for managing identities. */
@ConditionalOnProperty(prefix = "managementportal", name = ["legacyLogin"], havingValue = "false", matchIfMissing = true)
@Service
@Transactional
class IdentityService
    @Autowired
    constructor(
        private val managementPortalProperties: ManagementPortalProperties,
        private val authService: AuthService,
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
                        },
                    )
                }
            }

        private val adminUrl = managementPortalProperties.identityServer.serverAdminUrl
        private val publicUrl = managementPortalProperties.identityServer.serverUrl

        init {
            log.debug("Kratos serverUrl set to $publicUrl")
            log.debug("Kratos serverAdminUrl set to $adminUrl")
        }

        /**
         * Builds metadata for a user based on roles, authorities, and sources.
         */
        private fun buildMetadata(
            roles: Set<Role>,
            authorities: Set<String>,
            login: String,
            sources: List<String> = emptyList(),
        ): KratosSessionDTO.Metadata =
            try {
                KratosSessionDTO.Metadata(
                    aud = emptyList(),
                    sources = sources,
                    roles =
                        roles.mapNotNull { role ->
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
                    authorities = authorities,
                    scope =
                        Permission.scopes().filter { scope ->
                            authService.mayBeGranted(
                                roles.mapNotNull { it.role },
                                Permission.ofScope(scope),
                            )
                        },
                    mp_login = login,
                )
            } catch (e: Throwable) {
                val message = "Could not build metadata for user $login"
                log.error(message)
                throw IdpException(message, e)
            }

        private fun createIdentity(user: User): KratosSessionDTO.Identity =
            KratosSessionDTO.Identity(
                schema_id = "researcher",
                traits = KratosSessionDTO.Traits(email = user.email),
                metadata_public =
                    buildMetadata(
                        roles = user.roles,
                        authorities = user.authorities,
                        login = user.login!!,
                    ),
            )

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
                } else if (response.status.value == 409) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Identity for user ${user.login} already exists at the IDP. Continuing...")
                    }
                } else {
                    throw IdpException("Couldn't save Kratos ID to server at $adminUrl")
                }
            }

        @Throws(IdpException::class)
        suspend fun updateAssociatedIdentity(
            user: User,
            subject: Subject? = null,
        ): KratosSessionDTO.Identity =
            withContext(Dispatchers.IO) {
                val json = Json { ignoreUnknownKeys = true }
                val identityId =
                    user.identity
                        ?: subject?.externalId ?: throw IdpException("User has no identity")
                val sources = subject?.sources?.map { it.sourceId.toString() } ?: emptyList()
                val jsonPatchPayload = listOf(
                    JsonMetadataPatchOperation(
                        op = "replace",
                        path = "/metadata_public",
                        value = getIdentityMetadataWithRoles(user, sources)
                    )
                )
                val response =
                    httpClient.patch {
                        url("$adminUrl/admin/identities/$identityId")
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        setBody(json.encodeToString(jsonPatchPayload))
                    }

                if (response.status.isSuccess()) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Updated identity for user ${user.login} on IDP as ${it.id}")
                    }
                } else {
                    throw IdpException("Couldn't update identity on server at $adminUrl")
                }
            }

        @Throws(IdpException::class)
        suspend fun getExistingIdentity(identityId: String): KratosSessionDTO.Identity =
            withContext(Dispatchers.IO) {
                val response =
                    httpClient.get {
                        url("$adminUrl/admin/identities/$identityId")
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }

                if (response.status.isSuccess()) {
                    response.body<KratosSessionDTO.Identity>().also {
                        log.debug("Retrieved identity for ${it.id}")
                    }
                } else {
                    throw IdpException("Couldn't retrieve identity from server at $adminUrl")
                }
            }

        @Throws(IdpException::class)
        suspend fun getIdentityMetadataWithRoles(
            user: User,
            sources: List<String>,
        ): KratosSessionDTO.Metadata =
            withContext(Dispatchers.IO) {
                buildMetadata(
                    roles = user.roles,
                    authorities = user.authorities,
                    login = user.login!!,
                    sources = sources,
                )
            }

        @Throws(IdpException::class)
        suspend fun deleteAssociatedIdentity(userIdentity: String?) =
            withContext(Dispatchers.IO) {
                val identityId =
                    userIdentity
                        ?: throw IdpException(
                            "User with ID $userIdentity could not be deleted from the IDP. No identity was set",
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

        @Throws(IdpException::class)
        suspend fun sendActivationEmail(user: User): String =
            withContext(Dispatchers.IO) {
                val flowResponse =
                    httpClient
                        .get {
                            url("$publicUrl/self-service/verification/api")
                            contentType(ContentType.Application.Json)
                            accept(ContentType.Application.Json)
                        }.body<KratosSessionDTO.Verification>()

                val flowId = flowResponse.id

                if (flowId == null) {
                    throw IdpException("Failed to initiate verification flow for ${user.email}")
                }

                val activationResponse =
                    httpClient.post {
                        url("$publicUrl/self-service/verification?flow=$flowId")
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        setBody(mapOf("email" to user.email, "method" to "code"))
                    }

                if (!activationResponse.status.isSuccess()) {
                    throw IdpException("Failed to trigger verification email for ${user.email}")
                }

                flowId.also {
                    log.debug("Activation email sent for user ${user.login} with flow ID $it")
                }
            }

        companion object {
            private val log = LoggerFactory.getLogger(IdentityService::class.java)
        }
    }
