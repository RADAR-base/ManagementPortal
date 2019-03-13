package org.radarcns.management.web.rest;

import javax.annotation.PostConstruct;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.security.jwk.JavaWebKeySet;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.security.jwt.ManagementPortalOauthKeyStoreHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenKeyEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(TokenKeyEndpoint.class);

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    private ManagementPortalOauthKeyStoreHandler managementPortalOauthKeyStoreHandler;

    @PostConstruct
    public void init() {
        this.managementPortalOauthKeyStoreHandler =
                ManagementPortalOauthKeyStoreHandler.build(managementPortalProperties);
    }

    /**
     * Get the verification key for the token signatures. The principal has to
     * be provided only if the key is secret
     *
     * @return the key used to verify tokens
     */
    @GetMapping("/oauth/token_key")
    @Timed
    public JavaWebKeySet getKey() {
        logger.debug("Requesting verifier public keys...");
        return managementPortalOauthKeyStoreHandler.loadJwks();
    }

}
