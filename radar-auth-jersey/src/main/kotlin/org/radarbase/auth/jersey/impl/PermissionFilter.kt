/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.impl

import org.radarbase.auth.jersey.*
import org.radarbase.auth.jersey.inject.AuthenticationFilter
import org.radarcns.auth.authorization.Permission
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

/**
 * Check that the token has given permissions.
 */
class PermissionFilter : ContainerRequestFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    @Context
    private lateinit var auth: Auth

    @Context
    private lateinit var projectService: ProjectService

    @Context
    private lateinit var uriInfo: UriInfo

    override fun filter(requestContext: ContainerRequestContext) {
        val resourceMethod = resourceInfo.resourceMethod

        val annotation = resourceMethod.getAnnotation(NeedsPermission::class.java)

        val permission = Permission(annotation.entity, annotation.operation)

        val projectId = annotation.projectPathParam
                .takeIf { it.isNotEmpty() }
                ?.let { uriInfo.pathParameters[it] }
                ?.firstOrNull()
        val userId = annotation.userPathParam
                .takeIf { it.isNotEmpty() }
                ?.let { uriInfo.pathParameters[it] }
                ?.firstOrNull()

        val isAuthenticated = when {
            userId != null -> projectId != null && auth.token.hasPermissionOnSubject(permission, projectId, userId)
            projectId != null -> auth.token.hasPermissionOnProject(permission, projectId)
            else -> auth.token.hasPermission(permission)
        }

        if (!isAuthenticated) {
            abortWithForbidden(requestContext, permission)
            return
        }
        projectId?.let { projectService.ensureProject(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionFilter::class.java)

        /**
         * Abort the request with a forbidden status. The caller must ensure that no other changes are
         * made to the context (i.e., make a quick return).
         * @param requestContext context to abort
         * @param scope the permission that is needed.
         */
        fun abortWithForbidden(requestContext: ContainerRequestContext, scope: Permission) {
            val message = "$scope permission not given."
            logger.warn("[403] {}: {}",
                    requestContext.uriInfo.path, message)

            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .header("WWW-Authenticate", AuthenticationFilter.BEARER_REALM
                                    + " error=\"insufficient_scope\""
                                    + " error_description=\"$message\""
                                    + " scope=\"$scope\"")
                            .build())
        }
    }
}
