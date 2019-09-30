/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import com.auth0.jwt.JWT
import org.radarbase.auth.jersey.impl.ManagementPortalAuth
import org.radarcns.auth.authentication.TokenValidator
import org.radarcns.auth.config.TokenVerifierPublicKeyConfig
import org.radarcns.auth.exception.TokenValidationException
import org.slf4j.LoggerFactory
import java.net.URI
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Creates a TokenValidator based on the current management portal configuration. */
class ManagementPortalTokenValidator(
        @Context private val tokenValidator: TokenValidator) : AuthValidator {
    init {
        try {
            this.tokenValidator.refresh()
            logger.debug("Refreshed Token Validator keys")
        } catch (ex: Exception) {
            logger.error("Failed to immediately initialize token validator, will try again later: {}",
                    ex.toString())
        }
    }

    override fun verify(token: String, request: ContainerRequestContext): Auth? {
        val jwt = try {
            JWT.decode(token)!!
        } catch (ex: Exception) {
            throw TokenValidationException("JWT cannot be decoded")
        }
        tokenValidator.validateAccessToken(token)
        return ManagementPortalAuth(jwt)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManagementPortalTokenValidator::class.java)
    }
}
