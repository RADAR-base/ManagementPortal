package org.radarbase.management.web.rest

import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.security.JwtAuthenticationFilter.Companion.radarToken
import org.radarbase.management.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/api")
class LoginEndpoint @Autowired constructor(
    private val loginService: LoginService,
    private val managementPortalProperties: ManagementPortalProperties,
    private val tokenValidator: TokenValidator,
) {
    /**
     * OAuth2 login redirect endpoint.
     *
     * - If `code` is null: redirect the user to the IdP authorization endpoint.
     * - If `code` is present: exchange it for an access token, validate it, store it in the HTTP
     *   session, and then redirect to the frontend home page **without** exposing the access token
     *   in the URL.
     */
    @GetMapping("/redirect/login")
    suspend fun loginRedirect(
        @RequestParam(required = false) code: String?,
        session: HttpSession,
    ): RedirectView {
        val redirectView = RedirectView()

        if (code == null) {
            redirectView.url = loginService.buildAuthUrl()
        } else {
            try {
                val accessToken = loginService.fetchAccessToken(code)

                // Validate the token and store it in the HTTP session so that subsequent
                // frontend requests can authenticate using the session cookie, without
                // ever exposing the raw token in the browser URL.
                val radarToken = tokenValidator.validateBlocking(accessToken)
                session.radarToken = DataRadarToken(radarToken)

                val baseUrl = managementPortalProperties.common.managementPortalBaseUrl
                redirectView.url = "$baseUrl/#/"
            } catch (e: IdpException) {
                redirectView.url = "/error?message=Unable%20to%20authenticate"
            } catch (e: TokenValidationException) {
                redirectView.url = "/error?message=Unable%20to%20authenticate"
            }
        }
        return redirectView
    }

    @GetMapping("/redirect/account")
    fun settingsRedirect(): RedirectView {
        val redirectView = RedirectView()
        val baseUrl = managementPortalProperties.common.managementPortalBaseUrl
        redirectView.url = "$baseUrl/#/settings"
        return redirectView
    }
}
