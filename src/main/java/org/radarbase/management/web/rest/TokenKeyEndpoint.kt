package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.jwks.JsonWebKeySet
import org.radarbase.management.security.jwt.ManagementPortalOauthKeyStoreHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenKeyEndpoint @Autowired constructor(
    private val keyStoreHandler: ManagementPortalOauthKeyStoreHandler
) {
    @get:Timed
    @get:GetMapping("/oauth/token_key")
    val key: JsonWebKeySet
        /**
         * Get the verification key for the token signatures. The principal has to
         * be provided only if the key is secret
         *
         * @return the key used to verify tokens
         */
        get() {
            logger.debug("Requesting verifier public keys...")
            return keyStoreHandler.loadJwks()
        }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenKeyEndpoint::class.java)
    }
}
