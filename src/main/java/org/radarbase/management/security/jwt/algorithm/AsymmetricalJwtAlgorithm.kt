package org.radarbase.management.security.jwt.algorithm

import org.radarbase.auth.jwks.JsonWebKey
import org.radarbase.auth.jwks.MPJsonWebKey
import java.security.KeyPair
import java.util.*

abstract class AsymmetricalJwtAlgorithm protected constructor(protected val keyPair: KeyPair) : JwtAlgorithm {
    protected abstract val encodedStringHeader: String
    protected abstract val encodedStringFooter: String
    protected abstract val keyType: String
    override val verifierKeyEncodedString: String
        get() = """
             ${encodedStringHeader}
             ${String(Base64.getEncoder().encode(keyPair.public.encoded))}
             ${encodedStringFooter}
             """.trimIndent()
    override val jwk: JsonWebKey
        get() = MPJsonWebKey(
            algorithm.name,
            keyType,
            verifierKeyEncodedString
        )
}
