package org.radarbase.auth.kratos
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authentication.TokenVerifierLoader

class KratosTokenVerifierLoader(private val serverUrl: String) : TokenVerifierLoader {

    override suspend fun fetch(): List<TokenVerifier> {
        return listOf(
            KratosTokenVerifier(SessionService(serverUrl))
        )
    }

    override fun toString(): String = "KratosTokenKeyAlgorithmKeyLoader"
}
