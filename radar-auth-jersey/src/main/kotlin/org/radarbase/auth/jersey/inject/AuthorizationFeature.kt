/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.inject

import org.radarbase.auth.jersey.NeedsPermission
import org.radarbase.auth.jersey.impl.PermissionFilter
import org.slf4j.LoggerFactory
import javax.ws.rs.Priorities
import javax.ws.rs.container.DynamicFeature
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.ext.Provider

/** Authorization for different auth tags. */
@Provider
class AuthorizationFeature : DynamicFeature {
    override fun configure(resourceInfo: ResourceInfo, context: FeatureContext) {
        val resourceMethod = resourceInfo.resourceMethod
        if (resourceMethod.isAnnotationPresent(NeedsPermission::class.java)) {
            context.register(PermissionFilter::class.java, Priorities.AUTHORIZATION)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthorizationFeature::class.java)
    }
}
