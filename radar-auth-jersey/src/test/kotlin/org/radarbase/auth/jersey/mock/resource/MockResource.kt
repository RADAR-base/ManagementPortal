/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.mock.resource

import org.radarbase.auth.jersey.Auth
import org.radarbase.auth.jersey.Authenticated
import org.radarbase.auth.jersey.NeedsPermission
import org.radarcns.auth.authorization.Permission
import javax.annotation.Resource
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Path("/")
@Resource
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class MockResource {
    @GET
    fun something(): Map<String, String> {
        return mapOf("this" to "that")
    }

    @Authenticated
    @GET
    @Path("user")
    fun someUser(@Context auth: Auth): Map<String, String> {
        return mapOf("accessToken" to auth.token.token)
    }

    @Authenticated
    @GET
    @Path("projects/{projectId}/users/{subjectId}")
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "subjectId")
    fun mySubject(
            @PathParam("projectId") projectId: String,
            @PathParam("subjectId") userId: String): Map<String, String> {
        return mapOf("projectId" to projectId, "userId" to userId)
    }
}
