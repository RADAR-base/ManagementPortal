package org.radarbase.management.service

import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Period
import java.time.ZonedDateTime
import java.util.*
import java.util.Map
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.collections.HashSet
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.setOf

/**
 * Service class for managing users.
 */
@Service
@Transactional
open class UserService(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val passwordService: PasswordService,
    @Autowired private val roleService: RoleService,
    @Autowired private val userMapper: UserMapper,
    @Autowired private val revisionService: RevisionService,
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val authService: AuthService
) {

    /**
     * Activate a user with the given activation key.
     * @param key the activation key
     * @return an [Optional] which is populated with the activated user if the registration
     * key was found, and is empty otherwise.
     */
    fun activateRegistration(key: String): User {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key).let { user: User? ->
            // activate given user for the registration key.
            user?.activated = true
            user?.activationKey = null
            log.debug("Activated user: {}", user)
            user
        } ?: throw NotFoundException(
            "User with activation key $key not found",
            EntityName.USER,
            ErrorConstants.ERR_ENTITY_NOT_FOUND,
            Map.of("activationKey", key)
        )
    }

    /**
     * Update a user password with a given reset key.
     * @param newPassword the updated password
     * @param key the reset key
     * @return an [Optional] which is populated with the user whose password was reset if
     * the reset key was found, and is empty otherwise
     */
    fun completePasswordReset(newPassword: String, key: String): User? {
        log.debug("Reset user password for reset key {}", key)
        val user = userRepository.findOneByResetKey(key)
        val oneDayAgo = ZonedDateTime.now().minusSeconds(
            managementPortalProperties.common.activationKeyTimeoutInSeconds.toLong()
        )
        if (user?.resetDate?.isAfter(oneDayAgo) == true) user.password = passwordService.encode(newPassword)
        user?.resetKey = null
        user?.resetDate = null
        user?.activated = true

        return user
    }

    /**
     * Find the deactivated user and set the user's reset key to a new random value and set their
     * reset date to now.
     * Note: We do not use activation key for activating an account. It happens by resetting
     * generated password. Resetting activation is by resetting reset-key and reset-date to now.
     * @param login the login of the user
     * @return an [Optional] which holds the user if an deactivated user was found with the
     * given login, and is empty otherwise
     */
    fun requestActivationReset(login: String): User? {
        val user = userRepository.findOneByLogin(login)
        if (user?.activated != true) {
            user?.resetKey = passwordService.generateResetKey()
            user?.resetDate = ZonedDateTime.now()
        }

        return user
    }

    /**
     * Set a user's reset key to a new random value and set their reset date to now.
     * @param mail the email address of the user
     * @return an [Optional] which holds the user if an activated user was found with the
     * given email address, and is empty otherwise
     */
    fun requestPasswordReset(mail: String): User? {
        val user = userRepository.findOneByEmail(mail)
        if (user?.activated == true) user.resetKey = passwordService.generateResetKey()
        user?.resetDate = ZonedDateTime.now()
        return user
    }

    /**
     * Add a new user to the database.
     *
     *
     * The new user will not be activated and have a random password assigned. It is the
     * responsibility of the caller to make sure the new user has a means of activating their
     * account.
     * @param userDto the user information
     * @return the newly created user
     */
    @Throws(NotAuthorizedException::class)
    fun createUser(userDto: UserDTO): User {
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
        user.activated = false
        user.roles = userDto.roles?.let { getUserRoles(it, mutableSetOf()) }
        user = userRepository.save(user)
        log.debug("Created Information for User: {}", user)
        return user
    }

    @Throws(NotAuthorizedException::class)
    private fun getUserRoles(roleDtos: Set<RoleDTO>?, oldRoles: MutableSet<Role>): MutableSet<Role>? {
        if (roleDtos == null) {
            return null
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
                        e.organization(role.project?.organization?.name)
                    }
                    e.project(role.project?.projectName)
                }

                else -> throw IllegalStateException("Unknown authority scope.")
            }
        })
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     */
    fun updateUser(
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
                    Map.of("email", email)
                )
            }
        } else {
            user = userRepository.findOneByLogin(userName) ?: throw NotFoundException(
                "User with login $userName not found",
                EntityName.USER,
                ErrorConstants.ERR_ENTITY_NOT_FOUND,
                Map.of("user", userName)
            )
        }
        user.firstName = firstName
        user.lastName = lastName
        user.email = email
        user.langKey = langKey
        log.debug("Changed Information for User: {}", user)
        userRepository.save(user)
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDto user to update
     * @return updated user
     */
    @Transactional
    @Throws(NotAuthorizedException::class)
    open fun updateUser(userDto: UserDTO): UserDTO? {
        val userOpt = userDto.id?.let { userRepository.findById(it) }
        return if (userOpt?.isPresent == true) {
            var user = userOpt.get()
            user.firstName = userDto.firstName
            user.lastName = userDto.lastName
            user.email = userDto.email
            user.activated = userDto.isActivated
            user.langKey = userDto.langKey
            val managedRoles = user.roles
            val oldRoles = java.util.Set.copyOf(managedRoles)
            managedRoles?.clear()
            managedRoles?.addAll(getUserRoles(userDto.roles, oldRoles)!!)
            user = userRepository.save(user)
            log.debug("Changed Information for User: {}", user)
            userMapper.userToUserDTO(user)
        } else {
            null
        }
    }

    /**
     * Delete the user with the given login.
     * @param login the login to delete
     */
    fun deleteUser(login: String) {
        val user = userRepository.findOneByLogin(login)
        if (user != null) {
            userRepository.delete(user)
            log.debug("Deleted User: {}", user)
        }
        else {
            log.warn("could not delete User with login: {}", login)
        }
    }

    /**
     * Change the password of the user with the given login.
     * @param password the new password
     */
    fun changePassword(password: String) {
        val currentUser = SecurityUtils.currentUserLogin
            ?: throw InvalidRequestException(
                "Cannot change password of unknown user", "", ErrorConstants.ERR_ENTITY_NOT_FOUND
            )
        changePassword(currentUser, password)
    }

    /**
     * Change the user's password.
     * @param password the new password
     * @param login of the user to change password
     */
    fun changePassword(login: String, password: String) {
        val user = userRepository.findOneByLogin(login)

        if (user != null)
        {
            val encryptedPassword = passwordService.encode(password)
            user.password = encryptedPassword
            log.debug("Changed password for User: {}", user)
        }

    }

    /**
     * Get a page of users.
     * @param pageable the page information
     * @return the requested page of users
     */
    @Transactional(readOnly = true)
    open fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        log.debug("Request to get all Users")
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
            .map { user: User? -> userMapper.userToUserDTO(user) }
    }

    /**
     * Get the user with the given login.
     * @param login the login
     * @return an [Optional] which holds the user if one was found with the given login,
     * and is empty otherwise
     */
    @Transactional(readOnly = true)
    open fun getUserWithAuthoritiesByLogin(login: String): UserDTO? {
        return userMapper.userToUserDTO(userRepository.findOneWithRolesByLogin(login))
    }

    @get:Transactional(readOnly = true)
    open val userWithAuthorities: User?
        /**
         * Get the current user.
         * @return the currently authenticated user, or null if no user is currently authenticated
         */
        get() = SecurityUtils.currentUserLogin?.let { userRepository.findOneWithRolesByLogin(it) }

    /**
     * Not activated users should be automatically deleted after 3 days.
     *
     * This is scheduled to
     * get fired everyday, at 01:00 (am). This is aimed at users, not subjects. So filter our
     * users with *PARTICIPANT role and perform the action.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    fun removeNotActivatedUsers() {
        log.info("Scheduled scan for expired user accounts starting now")
        val cutoff = ZonedDateTime.now().minus(Period.ofDays(3))
        val authorities = Arrays.asList(
            RoleAuthority.PARTICIPANT.authority, RoleAuthority.INACTIVE_PARTICIPANT.authority
        )
        userRepository.findAllByActivatedAndAuthoritiesNot(false, authorities).stream()
            .filter { user: User? -> user?.let { revisionService.getAuditInfo(it).createdAt }!!.isBefore(cutoff) }
            .forEach { user: User ->
                try {
                    userRepository.delete(user)
                    log.info("Deleted not activated user after 3 days: {}", user.login)
                } catch (ex: DataIntegrityViolationException) {
                    log.error("Could not delete user with login " + user.login, ex)
                }
            }
    }

    /**
     * Find all user with given filter.
     *
     * @param userFilter filtering for users.
     * @param pageable paging information
     * @param includeProvenance whether to include created and modification fields.
     * @return page of users.
     */
    fun findUsers(
        userFilter: UserFilter, pageable: Pageable?, includeProvenance: Boolean
    ): Page<UserDTO>? {
        return pageable?.let {
            userRepository.findAll(userFilter, it)
                .map(if (includeProvenance) Function { user: User? -> userMapper.userToUserDTO(user) } else Function { user: User? ->
                    userMapper.userToUserDTONoProvenance(
                        user
                    )
                })
        }
    }

    /**
     * Update the roles of the given user.
     * @param login user login
     * @param roleDtos new roles to set
     * @throws NotAuthorizedException if the current user is not allowed to modify the roles
     * of the target user.
     */
    @Transactional
    @Throws(NotAuthorizedException::class)
    open fun updateRoles(login: String, roleDtos: Set<RoleDTO>?) {
        val user = userRepository.findOneByLogin(login)
            ?: throw NotFoundException(
                "User with login $login not found",
                EntityName.USER,
                ErrorConstants.ERR_ENTITY_NOT_FOUND,
                Map.of("user", login)
            )

        val managedRoles = user.roles
        val oldRoles = managedRoles?.toMutableSet()

        managedRoles?.clear()
        managedRoles?.addAll(roleDtos?.let { oldRoles?.let { oldroles -> getUserRoles(it, oldroles) } }!!)
            ?: throw Exception("could not add rolser for user: $user")
        userRepository.save(user)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
