/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import javax.inject.Singleton

/** Registration for authorization against a ManagementPortal. */
class ManagementPortalResourceEnhancer : JerseyResourceEnhancer {
    override fun enhance(resources: ResourceConfig, binder: AbstractBinder) {
        binder.bind(ManagementPortalTokenValidator::class.java)
                .to(AuthValidator::class.java)
                .`in`(Singleton::class.java)
    }
}
