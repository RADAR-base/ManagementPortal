/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class MPUser(
    @JsonProperty("login")
    val id: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val activated: Boolean = false,

    val langKey: String? = null,
    val createdBy: String? = null,

    val createdDate: ZonedDateTime? = null,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: ZonedDateTime? = null,
    val roles: List<MPRole> = listOf(),
    val authorities: List<String> = listOf(),
)
