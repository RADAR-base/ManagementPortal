/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.impl

import org.radarbase.auth.jersey.AuthConfig
import org.radarcns.auth.authentication.TokenValidator
import org.radarcns.auth.config.TokenVerifierPublicKeyConfig
import java.net.URI
import java.util.function.Supplier
import javax.ws.rs.core.Context

class TokenValidatorFactory(@Context private val config: AuthConfig) : Supplier<TokenValidator> {
    override fun get(): TokenValidator = try {
        TokenValidator()
    } catch (e: RuntimeException) {
        val cfg = TokenVerifierPublicKeyConfig().apply {
            publicKeyEndpoints = listOf(URI("${config.managementPortalUrl}/oauth/token_key"))
            resourceName = config.jwtResourceName
        }
        TokenValidator(cfg)
    }
}
