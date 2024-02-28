package org.radarbase.management.web.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.micrometer.core.annotation.Timed
import kotlinx.serialization.json.Json
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.kratos.SessionService
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import javax.servlet.http.HttpServletRequest


/**
 * REST controller for managing Sessions.
 */
@RestController
@RequestMapping("/api")
class SessionResource(managementPortalProperties: ManagementPortalProperties) {
    private lateinit var sessionService: SessionService

    init {
        sessionService = SessionService(managementPortalProperties.identityServer.publicUrl())
    }

    private val httpClient = HttpClient(CIO).config {
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

    /**
     * GET  /logout-url  : Gets a kratos logout url for the current session.
     *
     * @return the ResponseEntity with status 200 (Ok) and with body [String].
     */
    @GetMapping("/logout-url")
    @Timed
    @Throws(IdpException::class)
    suspend fun getLogoutUrl(httpRequest: HttpServletRequest): ResponseEntity<String> {
        val sessionToken = HeaderUtil.parseCookies(httpRequest.getHeader(HttpHeaders.COOKIE)).find { it.name == "ory_kratos_session" }
            ?.value

        return try {
            sessionToken ?: throw IdpException("no ory_kratos_session could be parsed from the headers")

            ResponseEntity
                .ok()
                .body(sessionService.getLogoutUrl(sessionToken))
        } catch (e: Throwable) {
            ResponseEntity.badRequest()
                .headers(e.message?.let {
                    HeaderUtil.createFailureAlert("NoSession",
                        it, "could not create logout url")
                })
                .body("")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SessionResource::class.java)
    }
}
