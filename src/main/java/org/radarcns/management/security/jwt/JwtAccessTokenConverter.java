package org.radarcns.management.security.jwt;

import java.util.Map;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * Interface of a JwtAccessTokenConverter functions.
 */
public interface JwtAccessTokenConverter extends TokenEnhancer, AccessTokenConverter {

    /**
     * Field name for token id.
     */
    String TOKEN_ID = AccessTokenConverter.JTI;

    /**
     * Field name for access token id.
     */
    String ACCESS_TOKEN_ID = AccessTokenConverter.ATI;

    /**
     * Decodes and verifies a JWT token string and extracts claims into a Map.
     * @param token string to decode.
     * @return claims of decoded token.
     */
    Map<String, Object> decode(String token);

    /**
     * Encodes token into JWT token.
     * @param accessToken to encode.
     * @param authentication of the token.
     * @return JWT token string.
     */
    String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication);

    /**
     * Checks whether a token is access-token or refresh-token.
     * Based on additional-information ati which contains the reference to access token of a
     * refresh token.
     */
    boolean isRefreshToken(OAuth2AccessToken token);

    /**
     * Algorithm to sign and verify the token.
     * @param algorithm to sign the JWT token
     */
    void setAlgorithm(Algorithm algorithm);

}
