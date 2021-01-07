/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

data class MPSourceData(
    private val id: Long,
    //Source data type.
    private val sourceDataType: String,
    private val sourceDataName: String? = null,
    //Default data frequency
    private val frequency: String? = null,
    //Measurement unit.
    private val unit: String? = null,
    // Define if the samples are RAW data or instead they the result of some computation
    private val processingState: String? = null,
    //  the storage
    private val dataClass: String? = null,
    private val keySchema: String? = null,
    private val valueSchema: String? = null,
    private val topic: String? = null,
    private val provider: String? = null,
    private val enabled: Boolean = true,
    private val sourceType: MPSourceType? = null
)
