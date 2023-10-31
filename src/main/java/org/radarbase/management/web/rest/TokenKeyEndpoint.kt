package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.jwks.JsonWebKeySet;
import org.radarbase.management.security.jwt.ManagementPortalOauthKeyStoreHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenKeyEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TokenKeyEndpoint.class);

    private final ManagementPortalOauthKeyStoreHandler keyStoreHandler;

    @Autowired
    public TokenKeyEndpoint(
            ManagementPortalOauthKeyStoreHandler keyStoreHandler
    ) {
        this.keyStoreHandler = keyStoreHandler;
    }

    /**
     * Get the verification key for the token signatures. The principal has to
     * be provided only if the key is secret
     *
     * @return the key used to verify tokens
     */
    @GetMapping("/oauth/token_key")
    @Timed
    public JsonWebKeySet getKey() {
        logger.debug("Requesting verifier public keys...");
        return keyStoreHandler.loadJwks();
    }
}
