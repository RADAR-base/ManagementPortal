package org.radarbase.management.web.rest

import org.radarbase.auth.exception.IdpException
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api")
class LoginEndpoint @Autowired constructor(
    private val loginService: LoginService,
    private val managementPortalProperties: ManagementPortalProperties
) {
    @GetMapping("/redirect/login")
    suspend fun loginRedirect(@RequestParam(required = false) code: String?): RedirectView {
        val redirectView = RedirectView()

        if (code == null) {
            redirectView.url = loginService.buildAuthUrl()
        } else {
            try {
                val accessToken = loginService.fetchAccessToken(code)
                val baseUrl = managementPortalProperties.common.managementPortalBaseUrl
                redirectView.url = "$baseUrl/#/?access_token=$accessToken"
            } catch (e: IdpException) {
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
