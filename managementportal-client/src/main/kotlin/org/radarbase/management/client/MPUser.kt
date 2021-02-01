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
    private val id: String,

    private val firstName: String? = null,
    private val lastName: String? = null,
    private val email: String? = null,
    private val activated: Boolean = false,

    private val langKey: String? = null,
    private val createdBy: String? = null,

    private val createdDate: ZonedDateTime? = null,
    private val lastModifiedBy: String? = null,
    private val lastModifiedDate: ZonedDateTime? = null,
    private val roles: List<MPRole> = listOf(),
    private val authorities: List<String> = listOf(),
)
