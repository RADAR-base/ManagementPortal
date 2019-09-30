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

interface JerseyResourceEnhancer {
    val packages: Array<String>
        get() = arrayOf()
    fun enhance(binder: AbstractBinder)
}
