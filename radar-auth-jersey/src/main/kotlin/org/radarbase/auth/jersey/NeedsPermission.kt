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

/**
 * Indicates that a method needs an authenticated user that has a certain permission.
 */
@Target(AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NeedsPermission(
        /**
         * Entity that the permission is needed on.
         */
        val entity: Permission.Entity,
        /**
         * Operation on given entity that the permission is needed for.
         */
        val operation: Permission.Operation,
        /** Project path parameter */
        val projectPathParam: String = "",
        /** User path parameter. */
        val userPathParam: String = ""
        )
