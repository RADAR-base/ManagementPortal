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
    private Set<String> autoApproveScopes;
    private Long accessTokenValiditySeconds;
    private Long refreshTokenValiditySeconds;
    private Set<String> authorities;
    private Set<String> registeredRedirectUri;
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
    public Set<String> getAutoApproveScopes() {
        return autoApproveScopes;
    }

    /**
     * Set the autoApproveScopes
     */
    public void setAutoApproveScopes(Set<String> autoApproveScopes) {
        this.autoApproveScopes = autoApproveScopes;
    }

    /**
     * Get the AccessTokenValidity
     *
     * @return
     */
    public Long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    /**
     * Set the accessTokenValiditySeconds
     */
    public void setAccessTokenValiditySeconds(Long accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    /**
     * Get the RefreshTokenValidity
     *
     * @return
     */
    public Long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    /**
     * Set the refreshTokenValiditySeconds
     */
    public void setRefreshTokenValiditySeconds(Long refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
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

    public Set<String> getRegisteredRedirectUri() {
        return registeredRedirectUri;
    }

    public void setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
        this.registeredRedirectUri = registeredRedirectUri;
    }

    public Map<String, String> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, String> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
