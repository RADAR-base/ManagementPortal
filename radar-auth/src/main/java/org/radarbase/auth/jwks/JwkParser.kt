package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm

interface JwkParser {
    fun parse(key: JsonWebKey): Algorithm
}
