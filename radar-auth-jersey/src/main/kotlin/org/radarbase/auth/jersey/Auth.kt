/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import com.auth0.jwt.interfaces.DecodedJWT
import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.token.JwtRadarToken
import org.radarcns.auth.token.RadarToken
import javax.ws.rs.core.Response

abstract class Auth(private val jwt: DecodedJWT): JwtRadarToken(jwt) {
    /** ID of the OAuth client. */
    // TODO: parse client ID from RADAR token. Pending MP 0.5.7.
    val clientId: String? = getClaim("client_id").asString()
    /** Default project to apply operations to. */
    abstract val defaultProject: String?
    /** User ID, if set in the authentication. This may be null if a client credentials grant type is used. */
    val userId: String?
        get() = subject?.takeUnless { it.isEmpty() }
    /** Whether client credentials are being used to interact with the resource. */

    /**
     * Check whether the current authentication has given permissions on a subject in a project.
     *
     * @throws HttpApplicationException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnSubject(permission: Permission, projectId: String?, userId: String?) {
        if (!hasPermissionOnSubject(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "user_id_missing", "Missing user ID in request")
                        )) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId with user $userId")
        }
    }

    /**
     * Check whether the current authentication has given permissions on a project.
     *
     * @throws HttpApplicationException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnProject(permission: Permission, projectId: String?) {
        if (!hasPermissionOnProject(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request")
                        )) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId")
        }
    }

    /**
     * Check whether the current authentication has given permissions.
     *
     * @throws HttpApplicationException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnSource(permission: Permission, projectId: String?, userId: String?, sourceId: String?) {
        if (!hasPermissionOnSource(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "user_id_missing", "Missing user ID in request"),
                        sourceId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "source_id_missing", "Missing source ID in request"))) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId with user $userId and source $sourceId")
        }
    }

    /**
     * Get a claim from the token used for this authentication.
     */
    fun getClaim(name: String) = jwt.getClaim(name)

    /**
     * Whether the current authentication is for a user with a role in given project.
     */
    abstract fun hasRole(projectId: String, role: String): Boolean
}
