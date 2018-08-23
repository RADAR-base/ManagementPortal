package org.radarcns.management.service.dto;

import java.net.URL;
import java.util.Objects;

public class TokenDTO {
    private final String refreshToken;

    private final URL baseUrl;

    private final URL privacyPolicyUrl;

    private final String clientSecret;

    /**
     * Create a meta-token using refreshToken, baseUrl of platform, and privacyPolicyURL for this
     * token.
     * @param refreshToken refreshToken.
     * @param baseUrl baseUrl of the platform
     * @param privacyPolicyUrl privacyPolicyUrl for this token.
     */
    public TokenDTO(String refreshToken, URL baseUrl, URL privacyPolicyUrl, String clientSecret) {
        this.refreshToken = refreshToken;
        this.baseUrl = baseUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.clientSecret = clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public URL getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TokenDTO that = (TokenDTO) o;
        return Objects.equals(refreshToken, that.refreshToken)
                && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(privacyPolicyUrl, that.privacyPolicyUrl)
                && Objects.equals(clientSecret, that.clientSecret);
    }

    @Override
    public int hashCode() {

        return Objects.hash(refreshToken, baseUrl, privacyPolicyUrl, clientSecret);
    }

    @Override
    public String toString() {
        return "TokenDTO{"
                + "refreshToken='" + refreshToken
                + ", baseUrl=" + baseUrl
                + ", privacyPolicyUrl=" + privacyPolicyUrl
                + ", clientSecret=" + clientSecret
                + '}';
    }
}
