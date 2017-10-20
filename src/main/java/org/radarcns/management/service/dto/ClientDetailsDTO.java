package org.radarcns.management.service.dto;

import java.util.Map;
import java.util.Set;

/**
 * Created by dverbeec on 7/09/2017.
 */
public class ClientDetailsDTO {
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private Set<String> resourceIds;
    private Set<String> authorizedGrantTypes;
    private Boolean autoApprove;
    private Long accessTokenValidity;
    private Long refreshTokenValidity;
    private Set<String> authorities;
    private Map<String, String> additionalInformation;

    public ClientDetailsDTO() {

    }

    /**
     * Get the ClientId
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the clientId
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get the ClientSecret
     *
     * @return
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set the clientSecret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Get the Scope
     *
     * @return
     */
    public Set<String> getScope() {
        return scope;
    }

    /**
     * Set the scope
     */
    public void setScope(Set<String> scope) {
        this.scope = scope;
    }

    /**
     * Get the ResourceIds
     *
     * @return
     */
    public Set<String> getResourceIds() {
        return resourceIds;
    }

    /**
     * Set the resourceIds
     */
    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    /**
     * Get the AuthorizedGrantTypes
     *
     * @return
     */
    public Set<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    /**
     * Set the authorizedGrantTypes
     */
    public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    /**
     * Get the AutoApprove
     *
     * @return
     */
    public Boolean getAutoApprove() {
        return autoApprove;
    }

    /**
     * Set the autoApprove
     */
    public void setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    /**
     * Get the AccessTokenValidity
     *
     * @return
     */
    public Long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    /**
     * Set the accessTokenValidity
     */
    public void setAccessTokenValidity(Long accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }

    /**
     * Get the RefreshTokenValidity
     *
     * @return
     */
    public Long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    /**
     * Set the refreshTokenValidity
     */
    public void setRefreshTokenValidity(Long refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /**
     * Get the Authorities
     *
     * @return
     */
    public Set<String> getAuthorities() {
        return authorities;
    }

    /**
     * Set the authorities
     */
    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Map<String, String> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, String> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
