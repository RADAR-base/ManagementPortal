package org.radarbase.management.security.jwt.algorithm

import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPair
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

class RsaJwtAlgorithm(keyPair: KeyPair) : AsymmetricalJwtAlgorithm(keyPair) {
    /** RSA JWT algorithm.  */
    init {
        require(keyPair.private is RSAPrivateKey) { "Cannot make RsaJwtAlgorithm with " + keyPair.private.javaClass }
    }

    override val algorithm: Algorithm
        get() = Algorithm.RSA256(
            keyPair.public as RSAPublicKey,
            keyPair.private as RSAPrivateKey
        )
    override val encodedStringHeader: String
        get() = "-----BEGIN PUBLIC KEY-----"
    override val encodedStringFooter: String
        get() = "-----END PUBLIC KEY-----"
    override val keyType: String
        get() = "RSA"
}
