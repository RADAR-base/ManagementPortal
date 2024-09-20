package org.radarbase.auth.jwks

import kotlinx.serialization.Serializable

@Serializable
data class JsonWebKeySet(
    val keys: List<JsonWebKey> = emptyList(),
)
