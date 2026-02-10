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
import org.radarbase.auth.kratos.KratosSessionDTO
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
import io.ktor.http.HttpStatusCode
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
    private val keycloakUserUrl = "$adminUrl/admin/realms/$realm/users/"

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
            // Create identity in Keycloak
            user.identity = saveAsIdentity(user).id
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
            updateAssociatedIdentity(user)
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

            // Update identity in Kratos
            try {
                updateAssociatedIdentity(user)
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

    // TODO setting the password via MP API is not supported. It should be handled with a redirect to IDP.
    override suspend fun changePassword(password: String) {
        throw UnsupportedOperationException("Setting the password via MP API is not supported.")
    }

    override suspend fun changePassword(login: String, password: String) {
        val user = userRepository.findOneByLogin(login) ?: throw Exception("No user found with login $login")
        val externalId = user.identity ?: throw Exception("User $login has no identity set")
        withContext(Dispatchers.IO) {
            val response = httpClient.put {
                url("$keycloakUserUrl/$externalId/reset-password")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(mapOf(
                    "type" to "password",
                    "value" to password,
                    "temporary" to false
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

            val response = httpClient.get {
                url(keycloakUserUrl)
                parameter("username", user.login)
                accept(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.NotFound) {
                user.identity = saveAsIdentity(user).id
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

        // Update identity in Kratos
        try {
            updateAssociatedIdentity(user)
        } catch (e: Throwable) {
            log.warn(e.message, e)
        }
    }

    @Throws(IdpException::class)
    override suspend fun sendActivationEmail(user: User) {
        // With Keycloak, we need to create the identity first before inviting the user via email.
        saveAsIdentity(user)
        withContext(Dispatchers.IO) {
            val response = httpClient.put {
                    url("$keycloakUserUrl/${user.identity}/execute-actions-email,")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(requiredUserActions)
                }

            if (!response.status.isSuccess()) {
                throw IdpException("Failed to trigger activation email for ${user.email}")
            }
            log.debug("Activation email sent for user ${user.login}.")
        }
    }

    // DONE
    private fun createIdentity(user: User): KeycloakUserDTO =
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
            realmRoles = user.roles.map(Role::toString).toList(),
        )

    // DONE
    @Throws(IdpException::class)
    suspend fun saveAsIdentity(user: User): KeycloakUserDTO =
        withContext(Dispatchers.IO) {
            val identity = createIdentity(user)
            val response = httpClient.post {
                url(keycloakUserUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(identity)
            }

            if (response.status.isSuccess()) {
                response.body<KeycloakUserDTO>().also {
                    log.debug("Saved identity for user ${user.login} Keycloak")
                }
            } else if (response.status.value == 409) {
                response.body<KeycloakUserDTO>().also {
                    log.debug("Identity for user ${user.login} already exists at the IDP. Continuing...")
                }
            } else {
                throw IdpException("Couldn't save Keycloak ID to server at $adminUrl")
            }
        }

    @Throws(IdpException::class)
    suspend fun updateAssociatedIdentity(user: User): KeycloakUserDTO =
        withContext(Dispatchers.IO) {
            val externalId = user.identity ?: throw IdpException("User has no Keycloak ID")
            val identity = createIdentity(user)
            val response = httpClient.put {
                url("$keycloakUserUrl/$externalId")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(json.encodeToString(identity))
            }

            if (response.status.isSuccess()) {
                response.body<KeycloakUserDTO>().also {
                    log.debug("Updated identity for user ${user.login} on IDP as ${it.id}")
                }
            } else {
                throw IdpException("Couldn't update identity on server at $adminUrl")
            }
        }

    @Throws(IdpException::class)
    suspend fun deleteAssociatedIdentity(externalId: String) =
        withContext(Dispatchers.IO) {
            assert(externalId.isNotBlank()) { "User identity could not be deleted from the IDP. No identity was set" }

            val response = httpClient.delete {
                url("$keycloakUserUrl/$externalId")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
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
