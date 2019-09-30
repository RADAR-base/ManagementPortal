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
import org.radarbase.auth.jersey.impl.TokenValidatorFactory
import org.radarcns.auth.authentication.TokenValidator
import javax.inject.Singleton

/**
 * Registration for authorization against a ManagementPortal.
 *
 * It requires managementPortalUrl and jwtResourceName to be set in the AuthConfig.
 */
class ManagementPortalResourceEnhancer : JerseyResourceEnhancer {
    override fun enhance(binder: AbstractBinder) {
        binder.bindFactory(TokenValidatorFactory::class.java)
                .to(TokenValidator::class.java)
                .`in`(Singleton::class.java)

        binder.bind(ManagementPortalTokenValidator::class.java)
                .to(AuthValidator::class.java)
                .`in`(Singleton::class.java)
    }
}
