package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.MetaTokenService
import org.radarbase.management.service.dto.TokenDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.MalformedURLException
import java.time.Duration

@RestController
@RequestMapping("/api")
class MetaTokenResource {
    @Autowired
    private val metaTokenService: MetaTokenService? = null

    @Autowired
    private val authService: AuthService? = null

    /**
     * GET /api/meta-token/:tokenName.
     *
     *
     * Get refresh-token available under this tokenName.
     *
     * @param tokenName the tokenName given after pairing the subject with client
     * @return the client as a [ClientPairInfoDTO]
     */
    @GetMapping("/meta-token/{tokenName:" + Constants.TOKEN_NAME_REGEX + "}")
    @Timed
    @Throws(
        MalformedURLException::class,
    )
    fun getTokenByTokenName(
        @PathVariable("tokenName") tokenName: String?,
    ): ResponseEntity<TokenDTO> {
        log.info("Requesting token with tokenName {}", tokenName)
        return ResponseEntity.ok().body(tokenName?.let { metaTokenService!!.fetchToken(it) })
    }

    /**
     * DELETE /api/meta-token/:tokenName.
     *
     *
     * Delete refresh-token available under this tokenName.
     *
     * @param tokenName the tokenName given after pairing the subject with client
     * @return the client as a [ClientPairInfoDTO]
     */
    @DeleteMapping("/meta-token/{tokenName:" + Constants.TOKEN_NAME_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class,
    )
    fun deleteTokenByTokenName(
        @PathVariable("tokenName") tokenName: String?,
    ): ResponseEntity<Void> {
        log.info("Requesting token with tokenName {}", tokenName)
        val metaToken = tokenName?.let { metaTokenService!!.getToken(it) }
        val subject = metaToken?.subject
        val project: String =
            subject!!
                .activeProject
                ?.projectName
                ?: throw NotAuthorizedException(
                    "Cannot establish authority of subject without active project affiliation.",
                )
        val user = subject.user!!.login
        authService!!.checkPermission(
            Permission.SUBJECT_UPDATE,
            { e: EntityDetails -> e.project(project).subject(user) },
        )
        metaTokenService?.delete(metaToken)
        return ResponseEntity.noContent().build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuthClientsResource::class.java)

        @JvmField
        val DEFAULT_META_TOKEN_TIMEOUT = Duration.ofHours(1)

        @JvmField
        val DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT = Duration.ofDays(31)
    }
}
