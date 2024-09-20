package org.radarbase.auth.kratos

import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authentication.TokenVerifierLoader

class KratosTokenVerifierLoader(
    private val serverUrl: String,
    private val requireAal2: Boolean,
) : TokenVerifierLoader {
    override suspend fun fetch(): List<TokenVerifier> =
        listOf(
            KratosTokenVerifier(SessionService(serverUrl), requireAal2),
        )

    override fun toString(): String = "KratosTokenKeyAlgorithmKeyLoader"
}
