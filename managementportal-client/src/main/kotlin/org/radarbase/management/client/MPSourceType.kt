/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import kotlinx.serialization.Serializable

@Serializable
data class MPSourceType(
    var id: Long,
    val producer: String,
    val model: String,
    val catalogVersion: String,
    val sourceTypeScope: String,
    val canRegisterDynamically: Boolean = false,
    val name: String? = null,
    val description: String? = null,
    val assessmentType: String? = null,
    val appProvider: String? = null,
    val sourceData: List<MPSourceData> = listOf(),
)
