package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.jwks.JsonWebKey.Companion.ALGORITHM_RSA
import org.radarbase.auth.jwks.PEMCertificateParser.Companion.parsePublicKey
import java.security.interfaces.RSAPublicKey

class RSAPEMCertificateParser : PEMCertificateParser {
    override val keyFactoryType: String
        get() = ALGORITHM_RSA
    override val jwtAlgorithm: String
        get() = "SHA256withRSA"
    override val keyHeader: String
        get() = "-----BEGIN PUBLIC KEY-----"

    override fun parseAlgorithm(publicKey: String): Algorithm =
        publicKey
            .parsePublicKey<RSAPublicKey>(keyFactoryType)
            .toAlgorithm()
}
