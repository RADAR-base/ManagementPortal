package org.radarcns.management.service.dto;

import java.net.URL;
import java.util.Objects;

/**
 * Created by dverbeec on 29/08/2017.
 */
public class ClientPairInfoDTO {

    private final String tokenName;

    private final URL tokenUrl;

    private final URL privacyPolicyUrl;

    /**
     * Initialize with the given refresh token.
     * @param tokenName the refresh token
     * @param tokenUrl the refresh token
     */
    public ClientPairInfoDTO(String tokenName, URL tokenUrl, URL privacyPolicyUrl) {
        if (tokenUrl == null) {
            throw new IllegalArgumentException("tokenUrl can not be null");
        }
        this.tokenName = tokenName;
        this.tokenUrl = tokenUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    public String getTokenName() {
        return tokenName;
    }

    public URL getTokenUrl() {
        return tokenUrl;
    }

    public URL getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientPairInfoDTO that = (ClientPairInfoDTO) o;
        return Objects.equals(tokenName, that.tokenName) && Objects.equals(tokenUrl, that.tokenUrl)
            && Objects.equals(privacyPolicyUrl, that.privacyPolicyUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tokenName, tokenUrl, privacyPolicyUrl);
    }

    @Override
    public String toString() {
        return "ClientPairInfoDTO{" + "tokenName='" + tokenName + '\'' + '}';
    }
}
