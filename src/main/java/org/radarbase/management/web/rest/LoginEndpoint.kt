package org.radarbase.management.web.rest

import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api")
class LoginEndpoint
    @Autowired
    constructor(
        @Autowired private val managementPortalProperties: ManagementPortalProperties,
    ) {
        @GetMapping("/redirect/login")
        fun loginRedirect(): RedirectView {
            val redirectView = RedirectView()
            redirectView.url = managementPortalProperties.identityServer.loginUrl +
                                "/login?return_to=" + managementPortalProperties.common.managementPortalBaseUrl
            return redirectView
        }

        @GetMapping("/redirect/account")
        fun settingsRedirect(): RedirectView {
            val redirectView = RedirectView()
            redirectView.url = managementPortalProperties.identityServer.loginUrl + "/settings"
            return redirectView
        }

        companion object {
            private val logger = LoggerFactory.getLogger(TokenKeyEndpoint::class.java)
        }
    }
