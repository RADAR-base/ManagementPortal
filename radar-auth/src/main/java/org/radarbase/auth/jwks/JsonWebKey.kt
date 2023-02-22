package org.radarbase.auth.jwks

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.auth.exception.InvalidPublicKeyException
import java.lang.IllegalArgumentException

/**
 * Represents the OAuth 2.0 JsonWebKey for token verification.
 */
@Serializable(with = JavaWebKeyPolymorphicSerializer::class)
sealed interface JsonWebKey {
    val alg: String?
    val kty: String
    /** X.509 Certificate Chain. */
    val x5c: List<String>
    /** X.509 Certificate SHA-1 thumbprint. */
    val x5t: String?

    companion object {
        const val ALGORITHM_RSA = "RSA"
        const val ALGORITHM_EC = "EC"
    }
}

object JavaWebKeyPolymorphicSerializer : JsonContentPolymorphicSerializer<JsonWebKey>(JsonWebKey::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out JsonWebKey> = if ("value" in element.jsonObject) {
        MPJsonWebKey.serializer()
    } else when (element.jsonObject["kty"]?.jsonPrimitive?.content) {
        "EC" -> ECDSAJsonWebKey.serializer()
        "RSA" -> RSAJsonWebKey.serializer()
        else -> throw SerializationException("Unknown JavaWebKey")
    }
}

@Serializable
data class RSAJsonWebKey(
    override val alg: String = HashSize.RS256.name,
    override val kty: String,
    val kid: String? = null,
    val use: String = "sig",
    /** RSA modulus. */
    val n: String,
    /** RSA public exponent. */
    val e: String = "AQAB",
    /** X.509 Certificate Chain. */
    override val x5c: List<String> = emptyList(),
    /** X.509 Certificate SHA-1 thumbprint. */
    override val x5t: String? = null,
) : JsonWebKey {
    fun keySize(): HashSize = HashSize.valueOf(alg.uppercase())

    enum class HashSize {
        RS256,
        RS384,
        RS512;
    }
}

@Serializable
data class ECDSAJsonWebKey(
    override val alg: String? = null,
    override val kty: String,
    val kid: String? = null,
    val use: String = "sig",
    /** ECDSA x coordinate. */
    val x: String,
    /** ECDSA y coordinate. */
    val y: String,
    /** ECDSA curve. */
    val crv: String,
    /** X.509 Certificate Chain. */
    override val x5c: List<String> = emptyList(),
    /** X.509 Certificate SHA-1 thumbprint. */
    override val x5t: String? = null,
) : JsonWebKey {
    fun curve(): Curve {
        if (alg != null) {
            return Curve.valueOf(alg.uppercase())
        }
        return when (crv) {
            "P-256" -> Curve.ES256
            "P-384" -> Curve.ES384
            "P-521", "P-512" -> Curve.ES512
            else -> throw InvalidPublicKeyException("Unknown EC crv $crv")
        }
    }

    enum class Curve(val ecStdName: String) {
        ES256("secp256r1"),
        ES384("secp384r1"),
        ES512("secp521r1");
    }
}

@Serializable
data class MPJsonWebKey(
    override val alg: String = "ES256",
    override val kty: String,
    /** PEM certificate value */
    val value: String,
    /** X.509 Certificate Chain. */
    override val x5c: List<String> = emptyList(),
    /** X.509 Certificate SHA-1 thumbprint. */
    override val x5t: String? = null,
) : JsonWebKey {
    constructor(alg: String, kty: String, value: String) : this(alg, kty, value, emptyList(), null)
}
