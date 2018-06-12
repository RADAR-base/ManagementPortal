package org.radarcns.management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
public class ManagementPortalProperties {

    private final Mail mail = new Mail();

    private final Frontend frontend = new Frontend();

    private final Oauth oauth = new Oauth();

    private final CatalogueServer catalogueServer = new CatalogueServer();

    public ManagementPortalProperties.Frontend getFrontend() {
        return frontend;
    }

    public ManagementPortalProperties.Mail getMail() {
        return mail;
    }

    public ManagementPortalProperties.Oauth getOauth() {
        return oauth;
    }

    public CatalogueServer getCatalogueServer() {
        return catalogueServer;
    }

    public static class Mail {

        private String from = "";

        private String baseUrl = "";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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
}
