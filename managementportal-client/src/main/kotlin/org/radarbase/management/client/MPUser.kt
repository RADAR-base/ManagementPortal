/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MPUser(
    @SerialName("login")
    val id: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val activated: Boolean = false,

    val langKey: String? = null,
    val createdBy: String? = null,

    /** ZonedDateTime. */
    val createdDate: String? = null,
    val lastModifiedBy: String? = null,
    /** ZonedDateTime. */
    val lastModifiedDate: String? = null,
    val roles: List<MPRole> = listOf(),
    val authorities: List<String> = listOf(),
)
