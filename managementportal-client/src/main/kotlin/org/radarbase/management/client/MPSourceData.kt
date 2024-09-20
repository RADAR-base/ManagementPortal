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
data class MPSourceData(
    val id: Long,
    // Source data type.
    val sourceDataType: String,
    val sourceDataName: String? = null,
    // Default data frequency
    val frequency: String? = null,
    // Measurement unit.
    val unit: String? = null,
    // Define if the samples are RAW data or instead they the result of some computation
    val processingState: String? = null,
    //  the storage
    val dataClass: String? = null,
    val keySchema: String? = null,
    val valueSchema: String? = null,
    val topic: String? = null,
    val provider: String? = null,
    val enabled: Boolean = true,
    val sourceType: MPSourceType? = null,
)
