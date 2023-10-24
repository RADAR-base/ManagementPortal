package org.radarbase.auth.kratos_session
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authentication.TokenVerifierLoader
import org.radarbase.auth.jwks.JwkParser

class KratosTokenVerifierLoader : TokenVerifierLoader {

    override suspend fun fetch(): List<TokenVerifier> {
        return listOf(
            KratosTokenVerifier()
        )
    }

    override fun toString(): String = "KratosTokenKeyAlgorithmKeyLoader"
}
