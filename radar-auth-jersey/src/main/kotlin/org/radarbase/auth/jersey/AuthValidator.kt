/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import org.radarbase.auth.jersey.inject.AuthenticationFilter
import org.radarcns.auth.exception.TokenValidationException
import javax.ws.rs.container.ContainerRequestContext

interface AuthValidator {
    @Throws(TokenValidationException::class)
    fun verify(token: String, request: ContainerRequestContext): Auth?

    fun getToken(request: ContainerRequestContext): String? {
        val authorizationHeader = request.getHeaderString("Authorization")

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(AuthenticationFilter.BEARER, ignoreCase = true)) {
            return null
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring(AuthenticationFilter.BEARER.length).trim { it <= ' ' }
    }
}
