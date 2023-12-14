package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.jwks.JsonWebKey

interface JwkParser {
    fun parse(key: JsonWebKey): Algorithm
}
