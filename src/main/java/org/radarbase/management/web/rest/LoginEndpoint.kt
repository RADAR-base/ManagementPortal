package org.radarbase.management.web.rest

import java.time.Instant
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api")
class LoginEndpoint
@Autowired
constructor(
        private val managementPortalProperties: ManagementPortalProperties,
        @Autowired private val authService: AuthService
) {

    @GetMapping("/redirect/login")
    suspend fun loginRedirect(
            @RequestParam(required = false) code: String?,
    ): RedirectView {
        val redirectView = RedirectView()

        if (code == null) {
            redirectView.url = buildAuthUrl()
        } else {
            val accessToken = authService.fetchAccessToken(code)
            redirectView.url =
                    "${managementPortalProperties.common.baseUrl}/#/?access_token=$accessToken"
        }
        return redirectView
    }

    @GetMapping("/redirect/account")
    fun settingsRedirect(): RedirectView {
        val redirectView = RedirectView()
        redirectView.url = "${managementPortalProperties.identityServer.loginUrl}/settings"
        return redirectView
    }

    private fun buildAuthUrl(): String {
        return "${managementPortalProperties.authServer.loginUrl}/oauth2/auth?" +
                "client_id=${managementPortalProperties.frontend.clientId}&" +
                "response_type=code&" +
                "state=${Instant.now()}&" +
                "audience=res_ManagementPortal&" +
                "scope=offline&" +
                "redirect_uri=${managementPortalProperties.common.baseUrl}/api/redirect/login"
    }
}
