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

data class MPRole(
    @JsonProperty("projectName")
    val projectId: String? = null,
    val authorityName: String
)
