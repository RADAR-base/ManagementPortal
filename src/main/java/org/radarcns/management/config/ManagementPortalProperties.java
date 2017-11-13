package org.radarcns.management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
public class ManagementPortalProperties {
    private final Mail mail = new Mail();

    private final Frontend frontend = new Frontend();

    public ManagementPortalProperties.Frontend getFrontend() {
        return frontend;
    }

    public ManagementPortalProperties.Mail getMail() {
        return mail;
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

        private String clientScopes = "";

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

        public String getClientScopes() {
            return clientScopes;
        }

        public void setClientScopes(String clientScopes) {

            this.clientScopes = clientScopes.replace(",", " ");
        }
    }

}
