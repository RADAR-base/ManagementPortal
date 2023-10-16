package org.radarbase.auth.authentication

/**
 * Factory to load a list of token verifiers.
 */
interface TokenVerifierLoader {
    /**
     * Fetch a list of token verifiers, possibly from an external resource.
     */
    suspend fun fetch(): List<TokenVerifier>
}
