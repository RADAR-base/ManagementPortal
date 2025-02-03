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
                    "${managementPortalProperties.common.managementPortalBaseUrl}/#/?access_token=$accessToken"
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
                "scope=SOURCEDATA.CREATE SOURCETYPE.UPDATE SOURCETYPE.DELETE AUTHORITY.UPDATE MEASUREMENT.DELETE PROJECT.READ AUDIT.CREATE USER.DELETE AUTHORITY.DELETE SUBJECT.DELETE MEASUREMENT.UPDATE SOURCEDATA.UPDATE SUBJECT.READ USER.UPDATE SOURCETYPE.CREATE AUTHORITY.READ USER.CREATE SOURCE.CREATE SOURCE.READ SUBJECT.CREATE ROLE.UPDATE ROLE.READ MEASUREMENT.READ PROJECT.UPDATE PROJECT.DELETE ROLE.DELETE SOURCE.DELETE SOURCETYPE.READ ROLE.CREATE SOURCEDATA.DELETE SUBJECT.UPDATE SOURCE.UPDATE PROJECT.CREATE AUDIT.READ MEASUREMENT.CREATE AUDIT.DELETE AUDIT.UPDATE AUTHORITY.CREATE USER.READ ORGANIZATION.READ ORGANIZATION.CREATE ORGANIZATION.UPDATE SOURCEDATA.READ&" +
                "redirect_uri=${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
    }
}
