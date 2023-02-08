package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

interface PEMCertificateParser {
    /**
     * The key factory type for keys that this algorithm can parse.
     * @return the key factory type
     */
    val keyFactoryType: String

    /**
     * Get the algorithm description as it will be reported by the server public key endpoint
     * (e.g. "SHA256withRSA" or "SHA256withEC").
     * @return the algorithm description
     */
    val jwtAlgorithm: String

    /**
     * Get the header for a PEM encoded key that this algorithm can parse.
     *
     * @return the header for a PEM encoded key that this algorithm can parse
     */
    val keyHeader: String

    /**
     * Build a verification algorithm based on the supplied public key.
     * @param publicKey the public key in PEM format
     * @return the verification algorithm
     */
    fun parseAlgorithm(publicKey: String): Algorithm

    companion object {
        /**
         * Parse a public key in PEM format.
         * @param publicKey the public key to parse
         * @return a PublicKey object representing the supplied public key
         */
        inline fun <reified T: PublicKey> String.parsePublicKey(keyFactoryType: String): T {
            val trimmedKey = replace("-----BEGIN ([A-Z]+ )?PUBLIC KEY-----".toRegex(), "")
                .replace("-----END ([A-Z]+ )?PUBLIC KEY-----".toRegex(), "")
                .trim { it <= ' ' }
            return try {
                val decodedPublicKey = Base64.getDecoder().decode(trimmedKey)
                val spec = X509EncodedKeySpec(decodedPublicKey)
                val kf = KeyFactory.getInstance(keyFactoryType)
                kf.generatePublic(spec) as T
            } catch (ex: Exception) {
                throw IllegalArgumentException("Cannot parse public key with key factory type $keyFactoryType", ex)
            }
        }
    }
}
