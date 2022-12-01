/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MPOAuth2AccessToken(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 0,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("user_id") val externalUserId: String? = null,
)
