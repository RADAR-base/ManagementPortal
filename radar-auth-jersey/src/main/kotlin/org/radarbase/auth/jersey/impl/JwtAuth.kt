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
import com.fasterxml.jackson.databind.JsonNode
import org.radarbase.auth.jersey.Auth
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.Entity
import org.radarcns.auth.token.JwtRadarToken
import org.radarcns.auth.token.RadarToken

/**
 * Parsed JWT for validating authorization of data contents.
 */
class JwtAuth(project: String?, private val jwt: DecodedJWT) : Auth {
    override val token: RadarToken = object : JwtRadarToken(jwt) {
        override fun hasPermission(permission: Permission) = scopes.contains(permission.scopeName())

        override fun hasPermissionOnProject(permission: Permission, projectId: String): Boolean {
            return hasPermission(permission) && (claimProject != null && projectId == claimProject)
        }

        override fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String): Boolean {
            return hasPermissionOnProject(permission, projectId)
                    && (userId == this@JwtAuth.userId || hasPermission(Permission(Entity.PROJECT, permission.operation)))
        }

        override fun hasPermissionOnSource(permission: Permission, projectId: String, userId: String, sourceId: String): Boolean {
            return hasPermissionOnSubject(permission, projectId, userId)
        }
    }

    private val claimProject = jwt.getClaim("project").asString()
    override val defaultProject = claimProject ?: project

    override fun hasRole(projectId: String, role: String) = projectId == defaultProject

    override fun getClaim(name: String) = jwt.getClaim(name).`as`(JsonNode::class.java)
}
