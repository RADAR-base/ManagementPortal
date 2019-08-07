/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.radarcns.auth.authorization.Permission
import javax.ws.rs.core.Response

interface Auth {
    val clientId: String?
    val defaultProject: String?
    val userId: String?
    val isClientCredentials: Boolean

    fun hasPermissionOnProject(permission: Permission, projectId: String): Boolean
    fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String): Boolean

    fun checkPermissionOnSubject(permission: Permission, projectId: String?, userId: String?) {
        if (!hasPermissionOnSubject(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "user_id_missing", "Missing user ID in request")
                        )) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId with user $userId")
        }
    }

    fun checkPermissionOnProject(permission: Permission, projectId: String?) {
        if (!hasPermissionOnProject(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request")
                        )) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId")
        }
    }

    fun checkPermissionOnSource(permission: Permission, projectId: String?, userId: String?, sourceId: String?) {
        if (!hasPermissionOnSource(permission,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "user_id_missing", "Missing user ID in request"),
                        sourceId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "source_id_missing", "Missing source ID in request"))) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId with user $userId and source $sourceId")
        }
    }

    fun hasRole(projectId: String, role: String): Boolean
    fun hasPermission(permission: Permission): Boolean
    fun hasPermissionOnSource(permission: Permission, projectId: String, userId: String, sourceId: String): Boolean
}
