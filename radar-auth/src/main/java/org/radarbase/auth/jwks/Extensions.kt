package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

fun RSAPublicKey.toAlgorithm(hashSize: RSAJsonWebKey.HashSize = RSAJsonWebKey.HashSize.RS256): Algorithm = when (hashSize) {
    RSAJsonWebKey.HashSize.RS256 -> Algorithm.RSA256(this, null)
    RSAJsonWebKey.HashSize.RS384 -> Algorithm.RSA384(this, null)
    RSAJsonWebKey.HashSize.RS512 -> Algorithm.RSA512(this, null)
}

fun ECPublicKey.toAlgorithm(): Algorithm {
    val keySize = when (val orderLength = params.order.bitLength()) {
        in 0 .. 256 -> ECDSAJsonWebKey.Curve.ES256
        in 257 .. 384 -> ECDSAJsonWebKey.Curve.ES384
        in 385 .. 521 -> ECDSAJsonWebKey.Curve.ES512
        else -> throw IllegalArgumentException("Unknown ECDSA order length $orderLength")
    }
    return toAlgorithm(keySize)
}

fun ECPublicKey.toAlgorithm(keySize: ECDSAJsonWebKey.Curve): Algorithm = when (keySize) {
    ECDSAJsonWebKey.Curve.ES256 -> Algorithm.ECDSA256(this, null)
    ECDSAJsonWebKey.Curve.ES384 -> Algorithm.ECDSA384(this, null)
    ECDSAJsonWebKey.Curve.ES512 -> Algorithm.ECDSA512(this, null)
}
