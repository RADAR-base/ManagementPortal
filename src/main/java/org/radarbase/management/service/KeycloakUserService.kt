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
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.IdpException
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.repository.filters.UserFilter
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.security.SecurityUtils
import org.radarbase.management.service.RoleService.Companion.getRoleAuthority
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.web.rest.errors.ConflictException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Function
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.KeycloakUserDTO

/**
 * Service class for managing users with Kratos identity provider integration.
 * This service implements the UserService interface while providing full Kratos IDP functionality.
 */
@Service
@Transactional
class KeycloakUserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val userMapper: UserMapper,
    private val revisionService: RevisionService,
    private val managementPortalProperties: ManagementPortalProperties,
    private val authService: AuthService
) : UserService {

    @Autowired
    lateinit var roleService: RoleService

    // Kratos HTTP client and configuration
    private val httpClient = HttpClient(CIO) {
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

    private val json = Json { ignoreUnknownKeys = true }
    private val adminUrl = managementPortalProperties.identityServer.serverAdminUrl
    private val publicUrl = managementPortalProperties.identityServer.serverUrl
    private val realm = managementPortalProperties.identityServer.realm
    private val requiredUserActions = "[\"VERIFY_EMAIL\", \"UPDATE_PROFILE\",\"CHANGE_PASSWORD\"]"
    private val keycloakUsersUrl = "$adminUrl/admin/realms/$realm/users"
    private val keycloakRolesUrl = "$adminUrl/admin/realms/$realm/roles"

    // Access token management for user-creation-service client (client credentials)
    @Serializable
    private data class TokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Long,
        @SerialName("token_type") val tokenType: String? = null,
        val scope: String? = null
    )

    // TODO replace the token request logic with some Spring managed mechanism.

    @Volatile
    private var serviceAccessToken: String? = null
    @Volatile
    private var serviceTokenExpiryEpochSeconds: Long = 0L

    private fun tokenEndpoint(): String =
        "$publicUrl/realms/$realm/protocol/openid-connect/token"

    private fun isTokenExpiringSoon(nowEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
        // Refresh 30 seconds before expiry as buffer
        return serviceAccessToken == null || nowEpochSeconds >= (serviceTokenExpiryEpochSeconds - 30)
    }

    private suspend fun requestNewClientCredentialsToken(): TokenResponse = withContext(Dispatchers.IO) {
        val clientId = managementPortalProperties.identityServer.userCreationService.clientId
        val clientSecret = managementPortalProperties.identityServer.userCreationService.clientSecret
        val response = httpClient.post(tokenEndpoint()) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", clientId)
                append("client_secret", clientSecret)
            }.formUrlEncode())
        }
        if (!response.status.isSuccess()) {
            val body = response.body<String>()
            log.error("Failed to obtain client credentials token: status={} body={}", response.status, body)
            throw IdpException("Failed to obtain access token from Keycloak: ${response.status}")
        }
        response.body()
    }

    suspend fun getUserCreationServiceAccessToken(): String {
        if (isTokenExpiringSoon()) {
            val token = requestNewClientCredentialsToken()
            serviceAccessToken = token.accessToken
            // compute expiry epoch seconds
            serviceTokenExpiryEpochSeconds = (System.currentTimeMillis() / 1000) + token.expiresIn
        }
        return serviceAccessToken!!
    }

    // Periodic refresh to keep token warm for services that call frequently
    @Scheduled(fixedDelay = 60_000)
    fun refreshServiceAccessTokenScheduled() {
        // Run best-effort; ignore failures
        try {
            if (isTokenExpiringSoon()) {
                // Launch a blocking call in a separate coroutine context
                kotlinx.coroutines.runBlocking {
                    val token = requestNewClientCredentialsToken()
                    serviceAccessToken = token.accessToken
                    serviceTokenExpiryEpochSeconds = (System.currentTimeMillis() / 1000) + token.expiresIn
                    log.debug("Refreshed user-creation-service token, expiresIn={}s", token.expiresIn)
                }
            }
        } catch (ex: Exception) {
            log.warn("Could not refresh user-creation-service access token: {}", ex.message)
        }
    }

    init {
        log.debug("Keycloak serverUrl set to $publicUrl")
        log.debug("Keycloak serverAdminUrl set to $adminUrl")
    }

    // UserService interface implementation
    override fun activateRegistration(key: String): User {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key).let { user: User? ->
            user?.activated = true
            user?.activationKey = null
            log.debug("Activated user: {}", user)
            user
        } ?: throw NotFoundException(
            "User with activation key $key not found",
            EntityName.USER,
            ErrorConstants.ERR_ENTITY_NOT_FOUND,
            mapOf(Pair("activationKey", key))
        )
    }

    override fun completePasswordReset(newPassword: String, key: String): User? {
        log.debug("Reset user password for reset key {}", key)
        val user = userRepository.findOneByResetKey(key)
        val oneDayAgo = ZonedDateTime.now().minusSeconds(
            managementPortalProperties.common.activationKeyTimeoutInSeconds.toLong()
        )
        return if (user?.resetDate?.isAfter(oneDayAgo) == true) {
            user.password = passwordService.encode(newPassword)
            user.resetKey = null
            user.resetDate = null
            user.activated = true
            user
        } else null
    }

    override fun requestActivationReset(login: String): User? {
        val user = userRepository.findOneByLogin(login)
        if (user?.activated != true) {
            user?.resetKey = passwordService.generateResetKey()
            user?.resetDate = ZonedDateTime.now()
        }
        return user
    }

    override fun requestPasswordReset(mail: String): User? {
        val user = userRepository.findOneByEmail(mail)
        return if (user?.activated == true) {
            user.resetKey = passwordService.generateResetKey()
            user.resetDate = ZonedDateTime.now()
            user
        } else null
    }

    @Throws(NotAuthorizedException::class)
    override suspend fun createUser(userDto: UserDTO): User {
        var user = User()
        user.setLogin(userDto.login)
        user.firstName = userDto.firstName
        user.lastName = userDto.lastName
        user.email = userDto.email
        if (userDto.langKey == null) {
            user.langKey = "en" // default language
        } else {
            user.langKey = userDto.langKey
        }
        user.password = passwordService.generateEncodedPassword()
        user.resetKey = passwordService.generateResetKey()
        user.resetDate = ZonedDateTime.now()
        user.activated = true
        user.roles = getUserRoles(userDto.roles, mutableSetOf())

        try {
            user.identity = ensureExternalIdentity(user)
        } catch (e: Throwable) {
            log.warn("could not save user ${user.login} as identity", e)
        }

        user = withContext(Dispatchers.IO) {
            userRepository.save(user)
        }

        log.debug("Created Information for User: {}", user)
        return user
    }

    override suspend fun updateUser(
        userName: String, firstName: String?, lastName: String?, email: String?, langKey: String?
    ) {
        val userWithEmail = email?.let { userRepository.findOneByEmail(it) }
        val user: User
        if (userWithEmail != null) {
            user = userWithEmail
            if (!user.login.equals(userName, ignoreCase = true)) {
                throw ConflictException(
                    "Email address $email already in use",
                    EntityName.USER,
                    ErrorConstants.ERR_EMAIL_EXISTS,
                    mapOf(Pair("email", email))
                )
            }
        } else {
            user = userRepository.findOneByLogin(userName) ?: throw NotFoundException(
                "User with login $userName not found",
                EntityName.USER,
                ErrorConstants.ERR_ENTITY_NOT_FOUND,
                mapOf(Pair("user", userName))
            )
        }
        user.firstName = firstName
        user.lastName = lastName
        user.email = email
        user.langKey = langKey
        log.debug("Changed Information for User: {}", user)
        userRepository.save(user)

        // Update identity in Kratos
        try {
            updateExternalIdentity(user)
        } catch (e: Throwable) {
            log.warn(e.message, e)
        }
    }

    @Transactional
    @Throws(NotAuthorizedException::class)
    override suspend fun updateUser(userDto: UserDTO): UserDTO? {
        val userOpt = userDto.id?.let { userRepository.findById(it) }
        return if (userOpt?.isPresent == true) {
            var user = userOpt.get()
            user.firstName = userDto.firstName
            user.lastName = userDto.lastName
            user.email = userDto.email
            user.activated = userDto.isActivated
            user.langKey = userDto.langKey
            user.identity = userDto.identity
            val managedRoles = user.roles
            val oldRoles = java.util.Set.copyOf(managedRoles)
            managedRoles.clear()
            managedRoles.addAll(getUserRoles(userDto.roles, oldRoles))
            user = userRepository.save(user)
            log.debug("Changed Information for User: {}", user)

            try {
                updateExternalIdentity(user)
            } catch (e: Throwable) {
                log.warn("could not update user ${user.login} with identity ${user.identity} from IDP", e)
            }

            userMapper.userToUserDTO(user)
        } else {
            null
        }
    }

    override suspend fun updateUserWithSources(login: String, sources: List<MinimalSourceDetailsDTO>): UserDTO? {
        val user = userRepository.findOneByLogin(login)
        // TODO Keycloak does not support storing source atm.
//        if (user != null) {
//            updateAssociatedIdentityWithSources(user, sources)
//        }
        return userMapper.userToUserDTO(user)
    }

    override suspend fun deleteUser(login: String) {
        val user = userRepository.findOneByLogin(login)
        if (user != null) {
            userRepository.delete(user)
            try {
                if (user.identity != null)
                    deleteAssociatedIdentity(user.identity!!)
                else
                    log.warn("User ${user.login} has no identity set. Cannot delete from IDP.")
            } catch (e: Throwable) {
                log.warn(e.message, e)
            }
            log.debug("Deleted User: {}", user)
        } else {
            log.warn("could not delete User with login: {}", login)
        }
    }

    override suspend fun changePassword(password: String) {
        throw UnsupportedOperationException("Setting the password via MP API is not supported.")
    }

    override suspend fun changePassword(login: String, password: String) {
        val user = userRepository.findOneByLogin(login) ?: throw Exception("No user found with login $login")
        val externalId = user.identity ?: throw Exception("User $login has no identity set")
        withContext(Dispatchers.IO) {
            val response = httpClient.put {
                url("$keycloakUsersUrl/$externalId/reset-password")
                bearerAuth(getUserCreationServiceAccessToken())
                contentType(ContentType.Application.Json)
                setBody(PasswordResetPayload(
                    value = password
                ))
            }

            if (!response.status.isSuccess()) {
                throw IdpException("Failed to update user password for $login")
            }

            log.debug("Updated password for user $login.")
        }
    }

    @Transactional
    override suspend fun setAdminEmail(email: String): UserDTO {
        val user = userRepository.findOneByLogin("admin")
            ?: throw Exception("No admin user found")

        user.email = email
        log.debug("Set admin email to: {}", email)

        if (user.identity == null) {
            user.identity = ensureExternalIdentity(user)
        } else {
            updateExternalIdentity(user)
        }

        return userMapper.userToUserDTO(user)
            ?: throw Exception("Admin user could not be converted to DTO")
    }

    /**
     * Change the admin user's password. Should only be called in application startup.
     * @param password the new admin password
     */
    override suspend fun updateAdminPassword(password: String) {
        if (password.isNotEmpty()) {
            log.info("Overriding admin password to configured password")
            changePassword("admin", password)
        } else {
            log.info("AdminPassword property is empty. Using default password...")
        }
    }

    @Transactional(readOnly = true)
    override fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        log.debug("Request to get all Users")
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
            .map { user: User? -> userMapper.userToUserDTO(user) }
    }

    @Transactional(readOnly = true)
    override fun getUserDtoWithAuthoritiesByLogin(login: String): UserDTO? {
        return userMapper.userToUserDTO(userRepository.findOneWithRolesByLogin(login))
    }

    @Transactional(readOnly = true)
    override fun getUserWithAuthoritiesByLogin(login: String): User? {
        return userRepository.findOneWithRolesByLogin(login)
    }

    @Transactional(readOnly = true)
    override fun getUserWithAuthorities(): User? {
        return SecurityUtils.currentUserLogin?.let { userRepository.findOneWithRolesByLogin(it) }
    }

    override fun findUsers(
        userFilter: UserFilter, pageable: Pageable?, includeProvenance: Boolean
    ): Page<UserDTO>? {
        return pageable?.let {
            userRepository.findAll(userFilter, it)
                .map(if (includeProvenance) Function { user: User? -> userMapper.userToUserDTO(user) } else Function { user: User? ->
                    userMapper.userToUserDTONoProvenance(user)
                })
        }
    }

    @Transactional
    @Throws(NotAuthorizedException::class)
    override suspend fun updateRoles(login: String, roleDtos: Set<RoleDTO>?) {
        val user = userRepository.findOneByLogin(login)
            ?: throw NotFoundException(
                "User with login $login not found",
                EntityName.USER,
                ErrorConstants.ERR_ENTITY_NOT_FOUND,
                mapOf(Pair("user", login))
            )

        val managedRoles = user.roles
        val oldRoles = managedRoles.toMutableSet()

        managedRoles.clear()
        roleDtos?.let { getUserRoles(it, oldRoles) }?.let { managedRoles.addAll(it) }
            ?: throw Exception("could not add roles for user: $user")
        userRepository.save(user)

        try {
            updateExternalIdentity(user)
        } catch (e: Throwable) {
            log.warn(e.message, e)
        }
    }

    @Throws(IdpException::class)
    override suspend fun sendActivationEmail(user: User) {
        // With Keycloak, we need to create the identity first before inviting the user via email.
        ensureExternalIdentity(user)
        withContext(Dispatchers.IO) {
            val response = httpClient.put {
                    url("$keycloakUsersUrl/${user.identity}/execute-actions-email")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    bearerAuth(getUserCreationServiceAccessToken())
                    setBody(requiredUserActions)
                }

            if (!response.status.isSuccess()) {
                throw IdpException("Failed to trigger activation email for ${user.email}")
            }
            log.debug("Activation email sent for user ${user.login}.")
        }
    }

    // DONE
    private suspend fun createIdentity(user: User): KeycloakUserDTO =
        KeycloakUserDTO(
            // We enforce the id of the user record so that we can keep reference to it in the
            // Radarbase user record.
            id = UUID.randomUUID().toString(),
            username = user.login!!,
            firstName = user.firstName ?: "",
            lastName = user.lastName ?: "",
            email = user.email ?: "",
            enabled = user.activated,
            emailVerified = false,
            realmRoles = ensureRealmRoles(user.roles),
        )

    private suspend fun ensureRealmRoles(roles: Set<Role>): Set<String> {
        val roleNames = roles.filter {
            it.authority != null
        }.mapNotNull {
            role: Role -> when (role.authority!!.name) {
                "ROLE_SYS_ADMIN" -> role.authority!!.name
                "ROLE_ORGANIZATION_ADMIN" -> "${role.organization}:${role.authority!!.name}"
                "ROLE_PROJECT_ADMIN", "ROLE_PARTICIPANT" -> "${role.project}:${role.authority!!.name}"
                else -> throw IllegalArgumentException("Unsupported role authority: ${role.authority!!.name}")
            }
        }.toSet()
        log.debug("Ensuring realm roles in external IDP: {}", roleNames)
        withContext(Dispatchers.IO) {
            roleNames.forEach {
                val response = httpClient.post {
                    url(keycloakRolesUrl)
                    contentType(ContentType.Application.Json)
                    bearerAuth(getUserCreationServiceAccessToken())
                    setBody(RoleRepresentation(it))
                }
                when (response.status) {
                    HttpStatusCode.Created -> log.debug("Created realm role $it in IDP")
                    HttpStatusCode.Conflict -> log.debug("Realm role $it already present in IDP. Skipping creation.")
                    HttpStatusCode.Forbidden -> throw IdpException("Forbidden to create realm role $it in IDP. Make sure the token has 'manage-realm' role.")
                    else -> throw IdpException("Failed to create realm role $it in IDP.")
                }
            }
        }
        log.debug("Realm roles are present in external IDP.")
        return roleNames
    }

    @Throws(IdpException::class)
    suspend fun ensureExternalIdentity(user: User): String =
        withContext(Dispatchers.IO) {
            assert(user.login != null) { "User login not set. Cannot create identity in IDP."}
            if (user.identity != null) return@withContext user.identity!!
            val identity = createIdentity(user)
            val response = httpClient.post {
                url(keycloakUsersUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                bearerAuth(getUserCreationServiceAccessToken())
                setBody(identity)
            }
            when (response.status) {
                HttpStatusCode.Created -> log.debug("Created identity for user {} on IDP as {}", user.login, identity)
                HttpStatusCode.Conflict -> log.debug("Identity for user {} already exists at the IDP. Continuing...", user.login)
                else -> throw IdpException("Couldn't create identity on server at $adminUrl")
            }
            getIdentityByUsername(user.login!!)?.id ?: throw IdpException("Identity not found after creation")
        }

    @Throws(IdpException::class)
    suspend fun updateExternalIdentity(user: User): Unit =
        withContext(Dispatchers.IO) {
            val externalId = user.identity ?: throw IdpException("User has no Keycloak ID")
            val identity = createIdentity(user)
            val response = httpClient.put {
                url("$keycloakUsersUrl/$externalId")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                bearerAuth(getUserCreationServiceAccessToken())
                setBody(json.encodeToString(identity))
            }

            if (response.status.isSuccess()) {
                log.debug("Updated identity for user {} on IDP as {}", user.login, identity)
            } else {
                throw IdpException("Couldn't update identity on server at $adminUrl")
            }
        }

    @Throws(IdpException::class)
    suspend fun getIdentityByUsername(login: String): KeycloakUserDTO? =
        withContext(Dispatchers.IO) {
            val response = httpClient.get {
                url("$keycloakUsersUrl?username=$login&exact=true&first")
                contentType(ContentType.Application.Json)
                bearerAuth(getUserCreationServiceAccessToken())
            }
            when (response.status) {
                HttpStatusCode.OK -> response.body<List<KeycloakUserDTO>>().firstOrNull()
                HttpStatusCode.Forbidden -> throw IdpException("Forbidden to get identity from IDP. Make sure the token has 'view-users' role.")
                else -> throw IdpException("Couldn't get identity from server at $adminUrl")
            }
        }

    @Throws(IdpException::class)
    suspend fun deleteAssociatedIdentity(externalId: String) =
        withContext(Dispatchers.IO) {
            assert(externalId.isNotBlank()) { "User identity could not be deleted from the IDP. No identity was set" }

            val response = httpClient.delete {
                url("$keycloakUsersUrl/$externalId")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                bearerAuth(getUserCreationServiceAccessToken())
            }

            if (response.status.isSuccess()) {
                log.debug("Deleted identity for user $externalId from IDP.")
            } else {
                throw IdpException("Couldn't delete identity from server at $adminUrl")
            }
        }


    // Scheduled method for cleanup (from DefaultUserService)
    @Scheduled(cron = "0 0 1 * * ?")
    override fun removeNotActivatedUsers() {
        log.info("Remove not activated users not supported for Kratos")
    }

    // Helper methods (from DefaultUserService)
    @Throws(NotAuthorizedException::class)
    private fun getUserRoles(roleDtos: Set<RoleDTO>?, oldRoles: MutableSet<Role>): MutableSet<Role> {
        if (roleDtos == null) {
            return mutableSetOf()
        }
        val roles = roleDtos.map { roleDto: RoleDTO ->
            val authority = getRoleAuthority(roleDto)
            when (authority.scope) {
                RoleAuthority.Scope.GLOBAL -> roleService.getGlobalRole(authority)
                RoleAuthority.Scope.ORGANIZATION -> roleService.getOrganizationRole(
                    authority, roleDto.organizationId!!
                )
                RoleAuthority.Scope.PROJECT -> roleService.getProjectRole(
                    authority, roleDto.projectId!!
                )
            }
        }.toMutableSet()
        checkAuthorityForRoleChange(roles, oldRoles)
        return roles
    }

    @Throws(NotAuthorizedException::class)
    private fun checkAuthorityForRoleChange(roles: Set<Role>, oldRoles: Set<Role>) {
        val updatedRoles = HashSet(roles)
        updatedRoles.removeAll(oldRoles)
        for (r in updatedRoles) {
            checkAuthorityForRoleChange(r)
        }
        val removedRoles = HashSet(oldRoles)
        removedRoles.removeAll(roles)
        for (r in removedRoles) {
            checkAuthorityForRoleChange(r)
        }
    }

    @Throws(NotAuthorizedException::class)
    private fun checkAuthorityForRoleChange(role: Role) {
        authService.checkPermission(Permission.ROLE_UPDATE, { e: EntityDetails ->
            when (role.role?.scope) {
                RoleAuthority.Scope.GLOBAL -> {}
                RoleAuthority.Scope.ORGANIZATION -> e.organization(role.organization?.name)
                RoleAuthority.Scope.PROJECT -> {
                    if (role.project?.organization != null) {
                        e.organization(role.project?.organizationName)
                    }
                    e.project(role.project?.projectName)
                }
                else -> throw IllegalStateException("Unknown authority scope.")
            }
        })
    }

    companion object {
        private val log = LoggerFactory.getLogger(KeycloakUserService::class.java)
    }
}

@Serializable
data class PasswordResetPayload(
    val type: String = "password",
    val value: String,
    val temporary: Boolean = false
)

@Serializable
data class RoleRepresentation(
    val name: String
)
