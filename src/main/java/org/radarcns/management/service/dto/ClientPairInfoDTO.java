package org.radarcns.management.service.dto;

/**
 * Created by dverbeec on 29/08/2017.
 */
public class ClientPairInfoDTO {
    private String managementPortalRefreshToken;
    private String clientRefreshToken;
    private String subjectId;

    public ClientPairInfoDTO(String managementPortalRefreshToken, String clientRefreshToken, String subjectId) {
        this.managementPortalRefreshToken = managementPortalRefreshToken;
        this.clientRefreshToken = clientRefreshToken;
        this.subjectId = subjectId;
    }

    public String getManagementPortalRefreshToken() {
        return managementPortalRefreshToken;
    }

    public void setManagementPortalRefreshToken(String managementPortalRefreshToken) {
        this.managementPortalRefreshToken = managementPortalRefreshToken;
    }

    public String getClientRefreshToken() {
        return clientRefreshToken;
    }

    public void setClientRefreshToken(String clientRefreshToken) {
        this.clientRefreshToken = clientRefreshToken;
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

        if (!managementPortalRefreshToken.equals(that.managementPortalRefreshToken)) return false;
        if (!clientRefreshToken.equals(that.clientRefreshToken)) return false;
        return subjectId.equals(that.subjectId);
    }

    @Override
    public int hashCode() {
        int result = managementPortalRefreshToken.hashCode();
        result = 31 * result + clientRefreshToken.hashCode();
        result = 31 * result + subjectId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClientPairInfoDTO{" +
            "managementPortalRefreshToken='" + managementPortalRefreshToken + '\'' +
            ", clientRefreshToken='" + clientRefreshToken + '\'' +
            ", subjectId='" + subjectId + '\'' +
            '}';
    }
}
