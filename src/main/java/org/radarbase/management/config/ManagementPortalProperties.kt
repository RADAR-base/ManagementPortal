package org.radarbase.management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
public class ManagementPortalProperties {

    private final IdentityServer identityServer = new IdentityServer();

    private final Mail mail = new Mail();

    private final Frontend frontend = new Frontend();

    private Oauth oauth = new Oauth();

    private final Common common = new Common();

    private final CatalogueServer catalogueServer = new CatalogueServer();

    private final Account account = new Account();

    private final SiteSettings siteSettings = new SiteSettings();

    public ManagementPortalProperties.Frontend getFrontend() {
        return frontend;
    }

    public IdentityServer getIdentityServer() {
        return identityServer;
    }

    public ManagementPortalProperties.Mail getMail() {
        return mail;
    }

    public ManagementPortalProperties.Oauth getOauth() {
        return oauth;
    }

    public void setOauth(ManagementPortalProperties.Oauth oauth) {
        this.oauth = oauth;
    }

    public CatalogueServer getCatalogueServer() {
        return catalogueServer;
    }

    public Common getCommon() {
        return common;
    }

    public Account getAccount() {
        return account;
    }

    public SiteSettings getSiteSettings() {
        return siteSettings;
    }

    public static class Account {
        private boolean enableExposeToken = false;

        public boolean getEnableExposeToken() {
            return enableExposeToken;
        }

        public void setEnableExposeToken(boolean enableExposeToken) {
            this.enableExposeToken = enableExposeToken;
        }
    }

    public static class Common {

        private String baseUrl = "";

        private String managementPortalBaseUrl = "";

        private String privacyPolicyUrl = "";

        private String adminPassword = "";

        private Integer activationKeyTimeoutInSeconds = 24 * 60 * 60; // 1 day

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getPrivacyPolicyUrl() {
            return privacyPolicyUrl;
        }

        public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
            this.privacyPolicyUrl = privacyPolicyUrl;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getManagementPortalBaseUrl() {
            return managementPortalBaseUrl;
        }

        public void setManagementPortalBaseUrl(String managementPortalBaseUrl) {
            this.managementPortalBaseUrl = managementPortalBaseUrl;
        }

        public Integer getActivationKeyTimeoutInSeconds() {
            return activationKeyTimeoutInSeconds;
        }

        public void setActivationKeyTimeoutInSeconds(Integer activationKeyTimeoutInSeconds) {
            this.activationKeyTimeoutInSeconds = activationKeyTimeoutInSeconds;
        }
    }

    public static class Mail {

        private String from = "";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

    }

    public static class Frontend {

        private String clientId = "";

        private String clientSecret = "";

        private Integer accessTokenValiditySeconds = 4 * 60 * 60;

        private Integer refreshTokenValiditySeconds = 72 * 60 * 60;

        private Integer sessionTimeout = 24 * 60 * 60; // a day

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

        public Integer getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(Integer sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public Integer getAccessTokenValiditySeconds() {
            return accessTokenValiditySeconds;
        }

        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        }

        public Integer getRefreshTokenValiditySeconds() {
            return refreshTokenValiditySeconds;
        }

        public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
            this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        }
    }

    public static class Oauth {

        private String clientsFile;

        private String signingKeyAlias;

        private List<String> checkingKeyAliases;

        private String keyStorePassword;

        private String metaTokenTimeout;

        private String persistentMetaTokenTimeout;

        private Boolean enablePublicKeyVerifiers = false;

        public String getClientsFile() {
            return clientsFile;
        }

        public void setClientsFile(String clientsFile) {
            this.clientsFile = clientsFile;
        }

        public String getSigningKeyAlias() {
            return signingKeyAlias;
        }

        public void setSigningKeyAlias(String signingKeyAlias) {
            this.signingKeyAlias = signingKeyAlias;
        }

        public List<String> getCheckingKeyAliases() {
            return checkingKeyAliases;
        }

        public void setCheckingKeyAliases(List<String> checkingKeyAliases) {
            this.checkingKeyAliases = checkingKeyAliases;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getMetaTokenTimeout() {
            return metaTokenTimeout;
        }

        public void setMetaTokenTimeout(String metaTokenTimeout) {
            this.metaTokenTimeout = metaTokenTimeout;
        }

        public String getPersistentMetaTokenTimeout() {
            return persistentMetaTokenTimeout;
        }

        public void setPersistentMetaTokenTimeout(String persistentMetaTokenTimeout) {
            this.persistentMetaTokenTimeout = persistentMetaTokenTimeout;
        }

        public Boolean getEnablePublicKeyVerifiers() {
            return enablePublicKeyVerifiers;
        }

        public void setEnablePublicKeyVerifiers(Boolean enablePublicKeyVerifiers) {
            this.enablePublicKeyVerifiers = enablePublicKeyVerifiers;
        }
    }

    public static class IdentityServer {
        private String serverUrl = null;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }
    }

    public static class CatalogueServer {

        private boolean enableAutoImport = false;

        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public boolean isEnableAutoImport() {
            return enableAutoImport;
        }

        public void setEnableAutoImport(boolean enableAutoImport) {
            this.enableAutoImport = enableAutoImport;
        }
    }

    public static class SiteSettings {

        private List<String> hiddenSubjectFields;

        public void setHiddenSubjectFields(List<String> hiddenSubjectFields) {
            this.hiddenSubjectFields = hiddenSubjectFields;
        }

        public List<String> getHiddenSubjectFields() {
            return hiddenSubjectFields;
        }
    }
}
