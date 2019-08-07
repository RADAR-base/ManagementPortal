/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.impl

import com.auth0.jwt.interfaces.DecodedJWT
import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.radarbase.auth.jersey.Auth
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.MEASUREMENT_CREATE
import org.radarcns.auth.token.RadarToken
import javax.ws.rs.core.Response

/**
 * Parsed JWT for validating authorization of data contents.
 */
class ManagementPortalAuth(jwt: DecodedJWT, private val token: RadarToken) : Auth {
    override val isClientCredentials
        get() = "client_credentials" == token.grantType

    // TODO: parse client ID from RADAR token. Pending MP 0.5.7.
    override val clientId: String? = jwt.getClaim("client_id").asString()
    override val defaultProject = token.roles.keys
            .firstOrNull { token.hasPermissionOnProject(MEASUREMENT_CREATE, it) }
    override val userId: String? = token.subject.takeUnless { it.isEmpty() }

    override fun hasPermissionOnProject(permission: Permission, projectId: String) = token.hasPermissionOnProject(permission, projectId)

    override fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String) = token.hasPermissionOnSubject(permission, projectId, userId)

    override fun hasPermissionOnSource(permission: Permission, projectId: String, userId: String, sourceId: String) = token.hasPermissionOnSource(permission, projectId, userId, sourceId)

    override fun hasRole(projectId: String, role: String) = token.roles
            .getOrDefault(projectId, emptyList())
            .contains(role)

    override fun hasPermission(permission: Permission) = token.hasPermission(permission)
}
