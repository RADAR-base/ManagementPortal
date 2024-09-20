package org.radarbase.auth.security.jwk

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.radarbase.auth.jwks.ECDSAJsonWebKey
import org.radarbase.auth.jwks.JsonWebKey

class JsonWebKeyTest {
    @Test
    fun deserialize() {
        val result = Json.decodeFromString<JsonWebKey>("""{"kty": "EC", "crv": "EC-512", "x": "abcd", "y": "cdef"}""")
        assertInstanceOf(ECDSAJsonWebKey::class.java, result)
        assertEquals(
            ECDSAJsonWebKey(
                kty = "EC",
                crv = "EC-512",
                x = "abcd",
                y = "cdef",
            ),
            result,
        )
    }
}
