package org.radarcns.management.service.dto;

import java.net.URL;
import java.util.Objects;

public class TokenDTO {
    private final String refreshToken;

    private final URL baseUrl;

    public TokenDTO(String refreshToken, URL baseUrl) {
        this.refreshToken = refreshToken;
        this.baseUrl = baseUrl;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TokenDTO tokenDTO = (TokenDTO) o;
        return Objects.equals(refreshToken, tokenDTO.refreshToken) && Objects
            .equals(baseUrl, tokenDTO.baseUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(refreshToken, baseUrl);
    }

    @Override
    public String toString() {
        return "TokenDTO{" + "refreshToken='" + refreshToken + '\'' + ", baseUrl=" + baseUrl + '}';
    }
}
