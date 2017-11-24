package org.radarcns.management.config;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
public class ManagementPortalProperties {
    private final Mail mail = new Mail();

    private final Frontend frontend = new Frontend();

    private final CatalogueServer catalogueServer = new CatalogueServer();

    public ManagementPortalProperties.Frontend getFrontend() {
        return frontend;
    }

    public ManagementPortalProperties.Mail getMail() {
        return mail;
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

        private Integer sessionTimeout = 24*60*60; // a day

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
