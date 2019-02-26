package org.radarcns.management.security.jwt;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public interface JwtAccessTokenConverter extends TokenEnhancer, AccessTokenConverter,
        InitializingBean {

    /**
     * Field name for token id.
     */
    String TOKEN_ID = AccessTokenConverter.JTI;

    /**
     * Field name for access token id.
     */
    String ACCESS_TOKEN_ID = AccessTokenConverter.ATI;

    Map<String, Object> decode(String token);

    String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication);

    boolean isRefreshToken(OAuth2AccessToken token);

}
