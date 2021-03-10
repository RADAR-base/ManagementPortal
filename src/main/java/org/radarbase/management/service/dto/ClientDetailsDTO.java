package org.radarbase.management.service.dto;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

/**
 * Created by dverbeec on 7/09/2017.
 */
public class ClientDetailsDTO {

    @NotNull
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Set<String> getScope() {
        return scope;
    }

    public void setScope(Set<String> scope) {
        this.scope = scope;
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Set<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    public Set<String> getAutoApproveScopes() {
        return autoApproveScopes;
    }

    public void setAutoApproveScopes(Set<String> autoApproveScopes) {
        this.autoApproveScopes = autoApproveScopes;
    }

    public Long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Long accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Long refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

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
