package org.radarbase.management.security.jwt

import com.auth0.jwt.algorithms.Algorithm
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.AccessTokenConverter
import org.springframework.security.oauth2.provider.token.TokenEnhancer

/**
 * Interface of a JwtAccessTokenConverter functions.
 */
interface JwtAccessTokenConverter :
    TokenEnhancer,
    AccessTokenConverter {
    /**
     * Decodes and verifies a JWT token string and extracts claims into a Map.
     * @param token string to decode.
     * @return claims of decoded token.
     */
    fun decode(token: String): Map<String, Any>

    /**
     * Encodes token into JWT token.
     * @param accessToken to encode.
     * @param authentication of the token.
     * @return JWT token string.
     */
    fun encode(
        accessToken: OAuth2AccessToken,
        authentication: OAuth2Authentication,
    ): String

    /**
     * Checks whether a token is access-token or refresh-token.
     * Based on additional-information ati which contains the reference to access token of a
     * refresh token.
     */
    fun isRefreshToken(token: OAuth2AccessToken): Boolean

    /**
     * Algorithm to sign and verify the token.
     * @param algorithm to sign the JWT token
     */
    fun setAlgorithm(algorithm: Algorithm)

    companion object {
        /**
         * Field name for token id.
         */
        const val TOKEN_ID = AccessTokenConverter.JTI

        /**
         * Field name for access token id.
         */
        const val ACCESS_TOKEN_ID = AccessTokenConverter.ATI
    }
}
