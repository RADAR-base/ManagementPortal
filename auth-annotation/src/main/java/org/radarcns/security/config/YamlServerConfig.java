package org.radarcns.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class YamlServerConfig implements ServerConfig {
    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";
    public static final String CONFIG_FILE_NAME = "radar-is.yml";
    private String tokenValidationEndpoint;
    private String publicKeyEndpoint;
    private String username;
    private String password;

    private Logger log = LoggerFactory.getLogger(YamlServerConfig.class);

    public YamlServerConfig() {
        log.info("YamlServerConfig initializing...");
    }

    public static YamlServerConfig readFromFileOrClasspath() throws IOException {
        Logger log = LoggerFactory.getLogger(YamlServerConfig.class);
        String customLocation = System.getenv(LOCATION_ENV);
        InputStream stream;
        if (customLocation != null) {
            log.info(LOCATION_ENV + " environment variable set, loading config from " +
                customLocation);
            stream = new FileInputStream(customLocation);
        }
        else {
            // if config location not defined, look for it on the classpath
            log.info(LOCATION_ENV + " environment variable not set, looking for it on"
                + " the classpath");
            stream = YamlServerConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE_NAME);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(stream, YamlServerConfig.class);
    }

    @Override
    public String getTokenValidationEndpoint() {
        return tokenValidationEndpoint;
    }

    public void setTokenValidationEndpoint(String tokenValidationEndpoint) {
        log.info("Token Validation Endpoint set to " + tokenValidationEndpoint);
        this.tokenValidationEndpoint = tokenValidationEndpoint;
    }

    public String getPublicKeyEndpoint() {
        return publicKeyEndpoint;
    }

    public void setPublicKeyEndpoint(String publicKeyEndpoint) {
        log.info("Token public key endpoint set to " + publicKeyEndpoint);
        this.publicKeyEndpoint = publicKeyEndpoint;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        log.info("Username set to " + username);
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        log.info("Password was set");
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlServerConfig)) return false;

        YamlServerConfig that = (YamlServerConfig) o;

        if (tokenValidationEndpoint != null ? !tokenValidationEndpoint.equals(that.tokenValidationEndpoint) : that.tokenValidationEndpoint != null)
            return false;
        if (publicKeyEndpoint != null ? !publicKeyEndpoint.equals(that.publicKeyEndpoint) : that.publicKeyEndpoint != null)
            return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;
        return password != null ? password.equals(that.password) : that.password == null;
    }

    @Override
    public int hashCode() {
        int result = tokenValidationEndpoint != null ? tokenValidationEndpoint.hashCode() : 0;
        result = 31 * result + (publicKeyEndpoint != null ? publicKeyEndpoint.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
