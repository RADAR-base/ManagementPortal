package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.security.JwtAuthenticationFilter.Companion.radarToken
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.MailService
import org.radarbase.management.service.PasswordService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.RadarWebApplicationException
import org.radarbase.management.web.rest.vm.KeyAndPasswordVM
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import javax.validation.Valid

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource(
    @Autowired private val userService: UserService,
    @Autowired private val mailService: MailService,
    @Autowired private val userMapper: UserMapper,
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val authService: AuthService,
    @Autowired private val passwordService: PasswordService
) {

    @Autowired(required = false)
    var token: RadarToken? = null

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500
     * (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    fun activateAccount(@RequestParam(value = "key") key: String): ResponseEntity<String> {
        return try {
            userService.activateRegistration(key)
            ResponseEntity<String>(HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * POST /login : check if the user is authenticated.
     *
     * @param session the HTTP session
     * @return user account details if the user is authenticated
     */
    @PostMapping("/login")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun login(session: HttpSession?): UserDTO? {
        if (token == null) {
            throw NotAuthorizedException("Cannot login without credentials")
        }
        log.debug("Logging in user to session with principal {}", token!!.username)
        session?.radarToken = DataRadarToken(token!!)
        return account
    }

    /**
     * POST /logout : log out.
     *
     * @param request the HTTP request
     * @return no content response if the user is authenticated
     */
    @PostMapping("/logout")
    @Timed
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        log.debug("Unauthenticate a user")
        val session = request.getSession(false)
        session?.invalidate()
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @get:Timed
    @get:GetMapping("/account")
    val account: UserDTO?
        /**
         * GET  /account : get the current user.
         *
         * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 401
         * (Internal Server Error) if the user couldn't be returned
         */
        get() {
            val currentUser = userService.userWithAuthorities
                ?: throw RadarWebApplicationException(
                        HttpStatus.FORBIDDEN,
                        "Cannot get account without user", EntityName.Companion.USER, ErrorConstants.ERR_ACCESS_DENIED
                    )
            val userDto = userMapper.userToUserDTO(currentUser)
            if (managementPortalProperties.account.enableExposeToken) {
                userDto?.accessToken = token!!.token
            }
            return userDto
        }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDto the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal
     * Server Error) if the user couldn't be updated
     */
    @PostMapping("/account")
    @Timed
    @Throws(NotAuthorizedException::class)
    suspend fun saveAccount(
        @RequestBody @Valid userDto: UserDTO,
        authentication: Authentication
    ): ResponseEntity<Void> {
        authService.checkPermission(Permission.USER_UPDATE, { e: EntityDetails ->
            e.user(userDto.login) })
        userService.updateUser(
            authentication.name, userDto.firstName,
            userDto.lastName, userDto.email, userDto.langKey
        )
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * POST  /account/change_password : changes the current user's password.
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new
     * password is not strong enough
     */
    @PostMapping(path = ["/account/change_password"], produces = [MediaType.TEXT_PLAIN_VALUE])
    @Timed
    fun changePassword(@RequestBody password: String): ResponseEntity<String> {
        passwordService.checkPasswordStrength(password)
        userService.changePassword(password)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * POST  /account/reset-activation/init : Resend the password activation email
     * to the user.
     *
     * @param login the login of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     * Request) if the email address is not registered or user is not deactivated
     */
    @PostMapping(path = ["/account/reset-activation/init"])
    @Timed
    fun requestActivationReset(@RequestBody login: String): ResponseEntity<Void> {
        val user = userService.requestActivationReset(login)
            ?: throw BadRequestException(
                    "Cannot find a deactivated user with login $login",
                    EntityName.Companion.USER, ErrorConstants.ERR_EMAIL_NOT_REGISTERED
                )

        mailService.sendCreationEmail(
            user, managementPortalProperties.common
                .activationKeyTimeoutInSeconds.toLong()
        )
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * POST   /account/reset_password/init : Email the user a password reset link.
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     * Request) if the email address is not registered
     */
    @PostMapping(path = ["/account/reset_password/init"])
    @Timed
    fun requestPasswordReset(@RequestBody mail: String): ResponseEntity<Void> {
        val user = userService.requestPasswordReset(mail)
            ?: throw BadRequestException(
                    "email address not registered",
                    EntityName.Companion.USER, ErrorConstants.ERR_EMAIL_NOT_REGISTERED
                )
        mailService.sendPasswordResetMail(user)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset, or status 400
     * (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = ["/account/reset_password/finish"], produces = [MediaType.TEXT_PLAIN_VALUE])
    @Timed
    fun finishPasswordReset(
        @RequestBody keyAndPassword: KeyAndPasswordVM
    ): ResponseEntity<Void?> {
        passwordService.checkPasswordStrength(keyAndPassword.newPassword)
        userService.completePasswordReset(keyAndPassword.newPassword, keyAndPassword.key)
            ?: return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)

        return ResponseEntity<Void?>(HttpStatus.NO_CONTENT)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccountResource::class.java)
    }
}
