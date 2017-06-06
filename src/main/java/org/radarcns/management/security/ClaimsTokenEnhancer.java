package org.radarcns.management.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        // TODO: retrieve coupled devices
        Map<String, Object> additionalInfo = new HashMap<>();
        authentication.getUserAuthentication().getDetails();
        additionalInfo.put("devices", Arrays.asList("a"));
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
