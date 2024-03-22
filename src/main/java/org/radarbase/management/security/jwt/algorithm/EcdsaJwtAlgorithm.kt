package org.radarbase.management.security.jwt.algorithm

import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

class EcdsaJwtAlgorithm(keyPair: KeyPair) : AsymmetricalJwtAlgorithm(keyPair) {
    /** ECDSA JWT algorithm.  */
    init {
        require(keyPair.private is ECPrivateKey) { "Cannot make EcdsaJwtAlgorithm with " + keyPair.private.javaClass }
    }

    override val algorithm: Algorithm
        get() = Algorithm.ECDSA256(
            keyPair.public as ECPublicKey,
            keyPair.private as ECPrivateKey
        )
    override val encodedStringHeader: String
        get() = "-----BEGIN EC PUBLIC KEY-----"
    override val encodedStringFooter: String
        get() = "-----END EC PUBLIC KEY-----"
    override val keyType: String
        get() = "EC"
}
