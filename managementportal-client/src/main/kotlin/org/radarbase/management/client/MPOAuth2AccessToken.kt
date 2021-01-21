/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class MPOAuth2AccessToken(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null,
    @JsonProperty("expires_in") val expiresIn: Long = 0,
    @JsonProperty("token_type") val tokenType: String? = null,
    @JsonProperty("user_id") val externalUserId: String? = null,
) {
    private val expiration: Instant = Instant.now() + Duration.ofSeconds(expiresIn) - Duration.ofMinutes(5)

    fun isValid() = Instant.now() < expiration
}
