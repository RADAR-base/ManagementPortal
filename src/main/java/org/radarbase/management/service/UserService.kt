package org.radarbase.management.service

import org.radarbase.auth.exception.IdpException
import org.radarbase.management.domain.User
import org.radarbase.management.repository.filters.UserFilter
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.UserDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO

/**
 * Interface for managing users.
 * This interface defines the contract for user management operations that can be implemented
 * by different user service implementations, including external user services.
 */
interface UserService {

    /**
     * Activate a user with the given activation key.
     * @param key the activation key
     * @return the activated user if the registration key was found
     * @throws NotFoundException if no user found with the activation key
     */
    fun activateRegistration(key: String): User

    /**
     * Update a user password with a given reset key.
     * @param newPassword the updated password
     * @param key the reset key
     * @return the user whose password was reset if the reset key was found, null otherwise
     */
    fun completePasswordReset(newPassword: String, key: String): User?

    /**
     * Find the deactivated user and set the user's reset key to a new random value and set their
     * reset date to now.
     * @param login the login of the user
     * @return the user if a deactivated user was found with the given login, null otherwise
     */
    fun requestActivationReset(login: String): User?

    /**
     * Set a user's reset key to a new random value and set their reset date to now.
     * @param mail the email address of the user
     * @return the user if an activated user was found with the given email address, null otherwise
     */
    fun requestPasswordReset(mail: String): User?

    /**
     * Add a new user to the database.
     * @param userDto the user information
     * @return the newly created user
     * @throws NotAuthorizedException if the current user is not authorized to create users
     */
    @Throws(NotAuthorizedException::class)
    suspend fun createUser(userDto: UserDTO): User

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     * @param userName the username of the user to update
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @throws ConflictException if email address is already in use by another user
     * @throws NotFoundException if user with given username is not found
     */
    suspend fun updateUser(
        userName: String, firstName: String?, lastName: String?, email: String?, langKey: String?
    )

    /**
     * Update all information for a specific user, and return the modified user.
     * @param userDto user to update
     * @return updated user DTO, or null if user not found
     * @throws NotAuthorizedException if the current user is not authorized to update the user
     */
    @Throws(NotAuthorizedException::class)
    suspend fun updateUser(userDto: UserDTO): UserDTO?

    /**
     * Update the user with the given sources.
     * @param userDto user to update
     * @return updated user DTO, or null if user not found
     */
    suspend fun updateUserWithSources(login: String, sources: List<MinimalSourceDetailsDTO>): UserDTO?

    /**
     * Delete the user with the given login.
     * @param login the login to delete
     */
    suspend fun deleteUser(login: String)

    /**
     * Change the password of the current user.
     * @param password the new password
     * @throws InvalidRequestException if no current user is authenticated
     */
    suspend fun changePassword(password: String)

    /**
     * Change the user's password.
     * @param login the login of the user to change password for
     * @param password the new password
     */
    suspend fun changePassword(login: String, password: String)

    /**
     * Change the admin user's password and set email. Should only be called in application startup.
     * @param email the new admin email
     * @return the updated admin user DTO
     * @throws Exception if no admin user found or conversion to DTO fails
     */
    suspend fun addAdminEmail(email: String): UserDTO

    /**
     * Change the admin user's password. Should only be called in application startup.
     * @param password the new admin password
     */
    suspend fun addAdminPassword(password: String)

    /**
     * Get a page of users.
     * @param pageable the page information
     * @return the requested page of users
     */
    fun getAllManagedUsers(pageable: Pageable): Page<UserDTO>

    /**
     * Get the user DTO with the given login.
     * @param login the login
     * @return the user DTO if one was found with the given login, null otherwise
     */
    fun getUserDtoWithAuthoritiesByLogin(login: String): UserDTO?

    /**
     * Get the user with the given login.
     * @param login the login
     * @return the user if one was found with the given login, null otherwise
     */
    fun getUserWithAuthoritiesByLogin(login: String): User?

    /**
     * Get the current user.
     * @return the currently authenticated user, or null if no user is currently authenticated
     */
    fun getUserWithAuthorities(): User?

    /**
     * Find all users with given filter.
     * @param userFilter filtering for users
     * @param pageable paging information
     * @param includeProvenance whether to include created and modification fields
     * @return page of users
     */
    fun findUsers(
        userFilter: UserFilter, pageable: Pageable?, includeProvenance: Boolean
    ): Page<UserDTO>?

    /**
     * Update the roles of the given user.
     * @param login user login
     * @param roleDtos new roles to set
     * @throws NotAuthorizedException if the current user is not allowed to modify the roles
     * @throws NotFoundException if user with given login is not found
     */
    @Throws(NotAuthorizedException::class)
    suspend fun updateRoles(login: String, roleDtos: Set<RoleDTO>?)

    /**
     * Send activation email to the user.
     * @param user the user to send activation email to
     * @return activation link or confirmation message
     * @throws IdpException if sending email fails
     * @throws IllegalStateException if no identity service is configured
     */
    @Throws(IdpException::class)
    suspend fun sendActivationEmail(user: User)

    /**
     * Remove not activated users.
     */
    fun removeNotActivatedUsers()
} 