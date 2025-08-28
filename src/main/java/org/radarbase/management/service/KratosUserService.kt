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
import org.radarbase.auth.kratos.KratosSessionDTO.JsonMetadataPatchOperation
import org.radarbase.auth.kratos.KratosSessionDTO.JsonStringPatchOperation
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
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Period
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Function
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO

/**
 * Service class for managing users with Kratos identity provider integration.
 * This service implements the UserService interface while providing full Kratos IDP functionality.
 */
@Service
@Transactional
class KratosUserService @Autowired constructor(
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

    init {
        log.debug("Kratos serverUrl set to $publicUrl")
        log.debug("Kratos serverAdminUrl set to $adminUrl")
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
            // Create identity in Kratos
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
        log.info("Kratos UserService does not support updating user with sources")
        val user = userRepository.findOneByLogin(login)
        if (user != null) {
            updateAssociatedIdentityWithSources(user, sources)
        }
        return userMapper.userToUserDTO(user)
    }

    override suspend fun deleteUser(login: String) {
        val user = userRepository.findOneByLogin(login)
        if (user != null) {
            userRepository.delete(user)
            
            // Delete identity from Kratos
            try {
                deleteAssociatedIdentity(user.identity)
            } catch (e: Throwable) {
                log.warn(e.message, e)
            }
            
            log.debug("Deleted User: {}", user)
        } else {
            log.warn("could not delete User with login: {}", login)
        }
    }

    override suspend fun changePassword(password: String) {
        val currentUser = SecurityUtils.currentUserLogin
            ?: throw InvalidRequestException(
                "Cannot change password of unknown user", "", ErrorConstants.ERR_ENTITY_NOT_FOUND
            )
        changePassword(currentUser, password)
    }

    override suspend fun changePassword(login: String, password: String) {
        val user = userRepository.findOneByLogin(login)

        if (user != null) {
            // Update password in Kratos
            try {
                updatePassword(user, password)
            } catch (e: Throwable) {
                log.warn("Failed to update password in Kratos for user ${user.login}", e)
            }
            
            user.password = passwordService.encode(password)
            log.debug("Changed password for User: {}", user)
        }
    }

    @Transactional
    override suspend fun addAdminEmail(email: String): UserDTO {
        val user = userRepository.findOneByLogin("admin")
            ?: throw Exception("No admin user found")

        user.email = email
        log.debug("Set admin email to: {}", email)

        // Create identity in Kratos if not exists
        user.identity = user.identity ?: saveAsIdentity(user).id

        return userMapper.userToUserDTO(user)
            ?: throw Exception("Admin user could not be converted to DTO")
    }

    /**
     * Change the admin user's password. Should only be called in application startup.
     * @param password the new admin password
     */
    override suspend fun addAdminPassword(password: String) {
        if (!password.isNullOrEmpty()) {
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
        sendKratosActivationEmail(user)
    }

    // Kratos-specific methods (from IdentityService)
    
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
                roles = roles.mapNotNull { role ->
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
                scope = Permission.scopes().filter { scope ->
                    roles.mapNotNull { it.role }.any { roleAuthority ->
                        authService.mayBeGranted(roleAuthority, Permission.ofScope(scope))
                    }
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
            schema_id = getSchemaIdFromUserRoles(user),
            traits = KratosSessionDTO.Traits(email = user.email),
            metadata_public = buildMetadata(
                roles = user.roles,
                authorities = user.authorities,
                login = user.login!!,
            ),
        )

    private fun getSchemaIdFromUserRoles(user: User): String {
        val roles = user.roles.map { it.role?.scope }.distinct()
        return when {
            roles.contains(RoleAuthority.Scope.GLOBAL) -> "admin"
            roles.contains(RoleAuthority.Scope.ORGANIZATION) -> "researcher"
            roles.contains(RoleAuthority.Scope.PROJECT) -> "researcher"
            else -> "researcher"
        }
    }

    @Throws(IdpException::class)
    suspend fun saveAsIdentity(user: User): KratosSessionDTO.Identity =
        withContext(Dispatchers.IO) {
            val identity = createIdentity(user)
            val response = httpClient.post {
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
    suspend fun updateAssociatedIdentity(user: User): KratosSessionDTO.Identity =
        withContext(Dispatchers.IO) {
            val identityId = user.identity ?: throw IdpException("User has no identity")
            val jsonPatchPayload = listOf(
                JsonMetadataPatchOperation(
                    op = "replace",
                    path = "/metadata_public",
                    value = buildMetadata(
                        roles = user.roles,
                        authorities = user.authorities,
                        login = user.login!!,
                    )
                )
            )
            val response = httpClient.patch {
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
    
    suspend fun updateAssociatedIdentityWithSources(user: User, sources: List<MinimalSourceDetailsDTO>): KratosSessionDTO.Identity =
        withContext(Dispatchers.IO) {
            val identityId = user.identity ?: throw IdpException("User has no identity")
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
        suspend fun getIdentityMetadataWithRoles(
            user: User,
            sources: List<MinimalSourceDetailsDTO>,
        ): KratosSessionDTO.Metadata =
            withContext(Dispatchers.IO) {
                buildMetadata(
                    roles = user.roles,
                    authorities = user.authorities,
                    login = user.login!!,
                    sources = sources.map { it.sourceId.toString() },
                )
            }


    @Throws(IdpException::class)
    suspend fun updatePassword(user: User, newPassword: String): KratosSessionDTO.Identity =
        withContext(Dispatchers.IO) {
            val identityId = user.identity ?: throw IdpException("User has no identity to update password")
            val encodedPassword = BCryptPasswordEncoder().encode(newPassword)
            log.debug("Updating password for user ${user.login}")
            val jsonPatchPayload = listOf(
                JsonStringPatchOperation(
                    op = "replace",
                    path = "/credentials/password/config/hashed_password",
                    value = encodedPassword,
                ),
            )
            val response = httpClient.patch {
                url("$adminUrl/admin/identities/$identityId")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(json.encodeToString(jsonPatchPayload))
            }

            if (response.status.isSuccess()) {
                response.body<KratosSessionDTO.Identity>().also {
                    log.debug("Updated password for user ${user.login} on IDP as ${it.id}")
                }
            } else {
                throw IdpException("Couldn't update password on server at $adminUrl")
            }
        }

    @Throws(IdpException::class)
    suspend fun deleteAssociatedIdentity(userIdentity: String?) =
        withContext(Dispatchers.IO) {
            val identityId = userIdentity
                ?: throw IdpException("User identity could not be deleted from the IDP. No identity was set")

            val response = httpClient.delete {
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
    suspend fun sendKratosActivationEmail(user: User): String =
        withContext(Dispatchers.IO) {
            val flowResponse = httpClient
                .get {
                    url("$publicUrl/self-service/verification/api")
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }.body<KratosSessionDTO.Verification>()

            val flowId = flowResponse.id
                ?: throw IdpException("Failed to initiate verification flow for ${user.email}")

            val activationResponse = httpClient.post {
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
        private val log = LoggerFactory.getLogger(KratosUserService::class.java)
    }
}