/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.mock

import org.radarbase.auth.jersey.ProjectService
import org.radarbase.auth.jersey.exception.HttpApplicationException
import javax.ws.rs.core.Response

class MockProjectService(private val projects: List<String>) : ProjectService {
    override fun ensureProject(projectId: String) {
        if (projectId !in projects) {
            throw HttpApplicationException(Response.Status.NOT_FOUND, "project_not_found")
        }
    }
}
