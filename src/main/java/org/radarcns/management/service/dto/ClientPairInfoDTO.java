package org.radarcns.management.service.dto;

/**
 * Created by dverbeec on 29/08/2017.
 */
public class ClientPairInfoDTO {
    private String refreshToken;
    private String subjectId;

    public ClientPairInfoDTO(String refreshToken, String subjectId) {
        this.refreshToken = refreshToken;
        this.subjectId = subjectId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientPairInfoDTO)) return false;

        ClientPairInfoDTO that = (ClientPairInfoDTO) o;

        if (!refreshToken.equals(that.refreshToken)) return false;
        return subjectId.equals(that.subjectId);
    }

    @Override
    public int hashCode() {
        int result = refreshToken.hashCode();
        result = 31 * result + subjectId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClientPairInfoDTO{" +
            "refreshToken='" + refreshToken + '\'' +
            ", subjectId='" + subjectId + '\'' +
            '}';
    }
}
