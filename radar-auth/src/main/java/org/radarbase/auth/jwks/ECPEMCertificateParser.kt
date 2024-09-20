package org.radarbase.auth.jwks

import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.jwks.JsonWebKey.Companion.ALGORITHM_EC
import org.radarbase.auth.jwks.PEMCertificateParser.Companion.parsePublicKey
import java.security.interfaces.ECPublicKey

class ECPEMCertificateParser : PEMCertificateParser {
    override val jwtAlgorithm: String
        get() = "SHA256withECDSA"
    override val keyHeader: String
        get() = "-----BEGIN EC PUBLIC KEY-----"

    override fun parseAlgorithm(publicKey: String): Algorithm =
        publicKey
            .parsePublicKey<ECPublicKey>(keyFactoryType)
            .toAlgorithm()

    override val keyFactoryType: String
        get() = ALGORITHM_EC
}
