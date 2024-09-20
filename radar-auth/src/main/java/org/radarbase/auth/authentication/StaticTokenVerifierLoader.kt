package org.radarbase.auth.authentication

/** Load token verifiers as a static object. */
class StaticTokenVerifierLoader(
    private val tokenVerifiers: List<TokenVerifier>,
) : TokenVerifierLoader {
    override suspend fun fetch(): List<TokenVerifier> = tokenVerifiers
}
