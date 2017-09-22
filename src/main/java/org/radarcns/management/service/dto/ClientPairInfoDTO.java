package org.radarcns.management.service.dto;

/**
 * Created by dverbeec on 29/08/2017.
 */
public class ClientPairInfoDTO {
    private String refreshToken;

    public ClientPairInfoDTO(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("refreshToken can not be null");
        }
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientPairInfoDTO)) return false;

        ClientPairInfoDTO that = (ClientPairInfoDTO) o;

        return refreshToken.equals(that.refreshToken);
    }

    @Override
    public int hashCode() {
        return refreshToken.hashCode();
    }
}
