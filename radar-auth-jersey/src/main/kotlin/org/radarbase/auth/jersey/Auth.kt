/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import org.radarcns.auth.authorization.Permission

interface Auth {
    val clientId: String?
    val defaultProject: String?
    val userId: String?

    fun hasPermissionOnProject(permission: Permission, projectId: String): Boolean
    fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String): Boolean
    fun checkPermission(permission: Permission, projectId: String?, userId: String?, sourceId: String?)
    fun hasRole(projectId: String, role: String): Boolean
    fun hasPermission(permission: Permission): Boolean
    fun hasPermissionOnSource(permission: Permission, projectId: String, userId: String, sourceId: String): Boolean
}
