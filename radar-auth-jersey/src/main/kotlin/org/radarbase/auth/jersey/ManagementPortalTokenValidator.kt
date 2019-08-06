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
import java.net.URI
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Creates a TokenValidator based on the current management portal configuration. */
class ManagementPortalTokenValidator(@Context config: AuthConfig) : AuthValidator {
    private val tokenValidator: TokenValidator = try {
        TokenValidator()
    } catch (e: RuntimeException) {
        val cfg = TokenVerifierPublicKeyConfig()
        cfg.publicKeyEndpoints = listOf(URI("${config.managementPortalUrl}/oauth/token_key"))
        cfg.resourceName = config.jwtResourceName
        TokenValidator(cfg)
    }

    override fun verify(token: String, request: ContainerRequestContext): Auth? {
        val jwt = try {
            JWT.decode(token)!!
        } catch (ex: Exception) {
            throw TokenValidationException("JWT cannot be decoded")
        }
        return ManagementPortalAuth(jwt, tokenValidator.validateAccessToken(token))
    }
}
