package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.User
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.repository.filters.UserFilter
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.InvalidRequestException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.radarbase.management.web.rest.util.PaginationUtil
import org.radarbase.management.web.rest.vm.ManagedUserVM
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.jhipster.web.util.ResponseUtil
import java.net.URISyntaxException
import java.util.*

/**
 * REST controller for managing users.
 *
 *
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 *
 *
 * For a normal use-case, it would be better to have an eager relationship between User and
 * Authority, and send everything to the client side: there would be no View Model and DTO, a lot
 * less code, and an outer-join which would be good for performance.
 *
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 *  * We want to keep a lazy association between the user and the authorities, because
 * people will quite often do relationships with the user, and we don't want them to get the
 * authorities all the time for nothing (for performance reasons). This is the #1 goal: we
 * should not impact our users' application because of this use-case.
 *  *  Not having an outer join causes n+1 requests to the database. This is not a real
 * issue as we have by default a second-level cache. This means on the first HTTP call we do
 * the n+1 requests, but then all authorities come from the cache, so in fact it's much
 * better than doing an outer join (which will get lots of data from the database, for each
 * HTTP call).
 *  *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 *
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
class UserResource(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val mailService: MailService,
    @Autowired private val userService: UserService,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val authService: AuthService
) {

    @Value("\${spring.profiles.active:}")
    lateinit var activeSpringProfiles: String

    /**
     * POST  /users  : Creates a new user.
     *
     * Creates a new user if the login and email are not
     * already used, and sends an mail with an activation link. The user needs to be activated on
     * creation.
     *
     * @param managedUserVm the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with
     * status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/users")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    suspend fun createUser(@RequestBody managedUserVm: ManagedUserVM): ResponseEntity<User?> {
        log.debug("REST request to save User : {}", managedUserVm)
        authService.checkPermission(Permission.USER_CREATE)
        return if (managedUserVm.id != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    EntityName.USER, "idexists", "A new user cannot already have an ID"
                )
            ).body(null)
            // Lowercase the user login before comparing with database
        } else if (managedUserVm.login?.lowercase().let { userRepository.findOneByLogin(it) } != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    EntityName.USER, "userexists", "Login already in use"
                )
            ).body(null)
        } else if (managedUserVm.email?.let { userRepository.findOneByEmail(it) } != null) {
            ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    EntityName.USER, "emailexists", "Email already in use"
                )
            ).body(null)
        } else {
            val newUser: User = userService.createUser(managedUserVm)
            if (activeSpringProfiles.contains("legacy-login"))
                mailService.sendCreationEmail(
                    newUser, managementPortalProperties.common.activationKeyTimeoutInSeconds.toLong()
                )
            else {
                userService.sendActivationEmail(newUser)
            }
            ResponseEntity.created(ResourceUriService.getUri(newUser)).headers(
                HeaderUtil.createAlert(
                    "userManagement.created", newUser.login
                )
            ).body(newUser)
        }
    }

    /**
     * PUT  /users : Updates an existing User.
     *
     * @param managedUserVm the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user, or with
     * status 400 (Bad Request) if the login or email is already in use, or with status 500
     * (Internal Server Error) if the user couldn't be updated
     */
    @PutMapping("/users")
    @Timed
    @Throws(NotAuthorizedException::class)
    suspend fun updateUser(@RequestBody managedUserVm: ManagedUserVM): ResponseEntity<UserDTO> {
        log.debug("REST request to update User : {}", managedUserVm)
        authService.checkPermission(Permission.USER_UPDATE, { e: EntityDetails -> e.user(managedUserVm.login) })
        var existingUser = managedUserVm.email?.let { userRepository.findOneByEmail(it) }
        if (existingUser != null && existingUser.id != managedUserVm.id) {
            throw BadRequestException("Email already in use", EntityName.USER, "emailexists")
        }
        existingUser = managedUserVm.login?.lowercase().let {
            userRepository.findOneByLogin(
                it
            )
        }
        if (existingUser != null && existingUser.id != managedUserVm.id) {
            throw BadRequestException("Login already in use", EntityName.USER, "emailexists")
        }
        val subject = subjectRepository.findOneWithEagerBySubjectLogin(managedUserVm.login)
        if (subject != null && managedUserVm.isActivated && subject.removed) {
            // if the subject is also a user, check if the removed/activated states are valid
            throw InvalidRequestException(
                "Subject cannot be the user to request " + "this changes", EntityName.USER, "error.invalidsubjectstate"
            )
        }
        val updatedUser: UserDTO?
        updatedUser = userService.updateUser(managedUserVm)
        return ResponseEntity.ok().headers(
            HeaderUtil.createAlert("userManagement.updated", managedUserVm.login)
        ).body(
            updatedUser
        )
    }

    /**
     * GET  /users : get all users.
     *
     * @param pageable   the pagination information
     * @param userFilter filter parameters as follows.
     * projectName Optional, if specified return only users associated this project
     * authority Optional, if specified return only users that have this authority
     * login Optional, if specified return only users that have this login
     * email Optional, if specified return only users that have this email
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getUsers(
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable?,
        userFilter: UserFilter,
        @RequestParam(defaultValue = "true") includeProvenance: Boolean
    ): ResponseEntity<List<UserDTO>> {
        authService.checkScope(Permission.USER_READ)

        val page = userService.findUsers(userFilter, pageable, includeProvenance)
        return ResponseEntity(
            page!!.content, PaginationUtil.generatePaginationHttpHeaders(page, "/api/users"), HttpStatus.OK
        )
    }

    /**
     * GET  /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with
     * status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getUser(@PathVariable login: String): ResponseEntity<UserDTO> {
        log.debug("REST request to get User : {}", login)
        authService.checkPermission(Permission.USER_READ, { e: EntityDetails -> e.user(login) })
        return ResponseUtil.wrapOrNotFound(
            Optional.ofNullable(userService.getUserDtoWithAuthoritiesByLogin(login))
        )
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    suspend fun deleteUser(@PathVariable login: String): ResponseEntity<Void> {
        log.debug("REST request to delete User: {}", login)
        authService.checkPermission(Permission.USER_DELETE, { e: EntityDetails -> e.user(login) })
        userService.deleteUser(login)
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build()
    }

    /**
     * Get /users/:login/roles : get the "login" User roles.
     *
     * @param login the login of the user to get roles from
     * @return the ResponseEntity with status 200 (OK)
     */
    @GetMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getUserRoles(@PathVariable login: String): ResponseEntity<Set<RoleDTO>> {
        log.debug("REST request to read User roles: {}", login)
        authService.checkPermission(Permission.ROLE_READ, { e: EntityDetails -> e.user(login) })
        return ResponseUtil.wrapOrNotFound(
            Optional.ofNullable(userService.getUserDtoWithAuthoritiesByLogin(login).let { obj: UserDTO? -> obj?.roles })
        )
    }

    /**
     * PUT /users/:login/roles : update the "login" User roles.
     *
     * @param login the login of the user to get roles from
     * @return the ResponseEntity with status 200 (OK)
     */
    @PutMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    suspend fun putUserRoles(
        @PathVariable login: String?, @RequestBody roleDtos: Set<RoleDTO>?
    ): ResponseEntity<Void> {
        log.debug("REST request to update User roles: {} to {}", login, roleDtos)
        authService.checkPermission(Permission.ROLE_UPDATE, { e: EntityDetails -> e.user(login) })
        userService.updateRoles(login!!, roleDtos)
        return ResponseEntity.noContent().build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserResource::class.java)
    }
}
