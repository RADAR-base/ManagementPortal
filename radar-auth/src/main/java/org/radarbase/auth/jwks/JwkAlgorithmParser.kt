package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.exception.InvalidPublicKeyException
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.jwks.JsonWebKey.Companion.ALGORITHM_EC
import org.radarbase.auth.jwks.JsonWebKey.Companion.ALGORITHM_RSA
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*
import java.util.*

class JwkAlgorithmParser(
    private val supportedAlgorithmsForWebKeySets: List<PEMCertificateParser>,
) : JwkParser {

    constructor() : this(listOf(ECPEMCertificateParser(), RSAPEMCertificateParser()))

    override fun parse(key: JsonWebKey): Algorithm {
        if (key.x5c.isNotEmpty()) {
            val x5cAlgorithm = supportedAlgorithmsForWebKeySets
                .firstNotNullOfOrNull { parser ->
                    try {
                        parser.parseAlgorithm(key.x5c[0])
                    } catch (ex: Exception) {
                        null
                    }
                }
            if (x5cAlgorithm != null) return x5cAlgorithm
        }

        return when (key) {
            is MPJsonWebKey -> supportedAlgorithmsForWebKeySets
                .firstOrNull { algorithm -> key.value.startsWith(algorithm.keyHeader) }
                ?.parseAlgorithm(key.value)
                ?: throw TokenValidationException("Unsupported public key: $key")
            is RSAJsonWebKey -> try {
                val kf: KeyFactory = KeyFactory.getInstance(ALGORITHM_RSA)
                val publicKeySpec = RSAPublicKeySpec(
                    BigInteger(1, Base64.getUrlDecoder().decode(key.n)),
                    BigInteger(1, Base64.getUrlDecoder().decode(key.e))
                )
                val publicKey = kf.generatePublic(publicKeySpec) as RSAPublicKey
                when (key.keySize()) {
                    RSAJsonWebKey.KeySize.RS256 -> Algorithm.RSA256(publicKey, null)
                    RSAJsonWebKey.KeySize.RS384 -> Algorithm.RSA384(publicKey, null)
                    RSAJsonWebKey.KeySize.RS512 -> Algorithm.RSA512(publicKey, null)
                }
            } catch (e: GeneralSecurityException) {
                throw InvalidPublicKeyException("Invalid public key", e)
            }
            is ECDSAJsonWebKey ->
                try {
                    val keyFactory = KeyFactory.getInstance(ALGORITHM_EC)
                    val keySize = key.keySize()
                    val ecPublicKeySpec = ECPublicKeySpec(
                        ECPoint(
                            BigInteger(1, Base64.getUrlDecoder().decode(key.x)),
                            BigInteger(1, Base64.getUrlDecoder().decode(key.y))
                        ),
                        AlgorithmParameters.getInstance(ALGORITHM_EC).run {
                            init(ECGenParameterSpec(keySize.ecStdName))
                            getParameterSpec(ECParameterSpec::class.java)
                        }
                    )
                    val publicKey = keyFactory.generatePublic(ecPublicKeySpec) as ECPublicKey
                    when (keySize) {
                        ECDSAJsonWebKey.KeySize.ES256 -> Algorithm.ECDSA256(publicKey, null)
                        ECDSAJsonWebKey.KeySize.ES384 -> Algorithm.ECDSA384(publicKey, null)
                        ECDSAJsonWebKey.KeySize.ES512 -> Algorithm.ECDSA512(publicKey, null)
                    }
                } catch (e: NoSuchAlgorithmException) {
                    throw InvalidPublicKeyException("Invalid algorithm to generate key", e)
                } catch (e: GeneralSecurityException) {
                    throw InvalidPublicKeyException("Invalid public key", e)
                }
        }
    }

    override fun toString(): String = buildString(50) {
        append("StringAlgorithmKeyLoader<algorithms=")
        supportedAlgorithmsForWebKeySets.joinTo(buffer = this, separator = ", ") {
            it.keyFactoryType
        }
        append('>')
    }
}
