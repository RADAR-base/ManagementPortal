/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.impl

import org.radarbase.auth.jersey.Auth
import java.util.function.Supplier
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Generates radar tokens from the security context. */
class AuthFactory : Supplier<Auth> {
    @Context
    private lateinit var context: ContainerRequestContext

    override fun get() = (context.securityContext as? RadarSecurityContext)?.auth
                ?: throw IllegalStateException("Created null wrapper")
}
