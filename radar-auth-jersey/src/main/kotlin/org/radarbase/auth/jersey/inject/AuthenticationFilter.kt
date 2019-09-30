/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.inject

import org.radarbase.auth.jersey.AuthValidator
import org.radarbase.auth.jersey.Authenticated
import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.radarbase.auth.jersey.impl.RadarSecurityContext
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.exception.TokenValidationException
import org.slf4j.LoggerFactory
import javax.annotation.Priority
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

/**
 * Authenticates user by a JWT in the bearer signed by the Management Portal.
 */
@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter : ContainerRequestFilter {
    @Context
    private lateinit var validator: AuthValidator

    override fun filter(requestContext: ContainerRequestContext) {
        val rawToken = validator.getToken(requestContext)

        if (rawToken == null) {
            logger.warn("[401] {}: No token bearer header provided in the request",
                    requestContext.uriInfo.path)
            throw HttpApplicationException(
                    status = Response.Status.UNAUTHORIZED,
                    code = "token_missing",
                    detailedMessage = "No bearer token is provided in the request.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))
        }

        val radarToken = try {
            validator.verify(rawToken, requestContext)
        } catch (ex: TokenValidationException) {
            logger.warn("[401] {}: {}", requestContext.uriInfo.path, ex.toString())
            throw HttpApplicationException(
                    status = Response.Status.UNAUTHORIZED,
                    code = "token_unverified",
                    detailedMessage = "Cannot verify token. It may have been rendered invalid.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM
                            + " error=\"invalid_token\""
                            + " error_description=\"${ex.message}\""))
        }

        if (radarToken == null) {
            logger.warn("[401] {}: Bearer token invalid",
                    requestContext.uriInfo.path)
            throw HttpApplicationException(
                    status = Response.Status.UNAUTHORIZED,
                    code = "token_invalid",
                    detailedMessage = "Bearer token is not a valid JWT.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))
        } else {
            requestContext.securityContext = RadarSecurityContext(radarToken)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationFilter::class.java)

        const val BEARER_REALM: String = "Bearer realm=\"Kafka REST Proxy\""
        const val BEARER = "Bearer "

        fun getInvalidScopeChallenge(message: String) = BEARER_REALM +
                " error=\"insufficient_scope\"" +
                " error_description=\"$message\"" +
                " scope=\"${Permission.MEASUREMENT_CREATE.scopeName()}\""
    }
}
