package org.radarcns.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarcns.auth.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class YamlServerConfig implements ServerConfig {
    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";
    public static final String CONFIG_FILE_NAME = "radar-is.yml";
    private URI mpBaseURI;
    private String resourceName;

    private static YamlServerConfig config;
    private Logger log = LoggerFactory.getLogger(YamlServerConfig.class);

    public YamlServerConfig() {
        log.info("YamlServerConfig initializing...");
    }

    public static YamlServerConfig readFromFileOrClasspath() {
        if (config != null) {
            return config;
        }
        Logger log = LoggerFactory.getLogger(YamlServerConfig.class);
        String customLocation = System.getenv(LOCATION_ENV);
        URL configFile;
        try {
            if (customLocation != null) {
                log.info(LOCATION_ENV + " environment variable set, loading config from " +
                    customLocation);
                configFile = new File(customLocation).toURI().toURL();
            } else {
                // if config location not defined, look for it on the classpath
                log.info(LOCATION_ENV + " environment variable not set, looking for it on"
                    + " the classpath");
                configFile = YamlServerConfig.class.getClassLoader().getResource(CONFIG_FILE_NAME);
                log.info("Config file found at " + configFile.getPath());
            }
        }
        catch (MalformedURLException ex) {
            throw new ConfigurationException(ex);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream stream = configFile.openStream()) {
            return mapper.readValue(stream, YamlServerConfig.class);
        }
        catch (IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    public static YamlServerConfig reloadConfig() throws IOException {
        config = null;
        return readFromFileOrClasspath();
    }

    public URI getMpBaseURI() {
        return mpBaseURI;
    }

    public void setMpBaseURI(URI mpBaseURI) {
        log.info("Token public key endpoint set to " + mpBaseURI.toString());
        this.mpBaseURI = mpBaseURI;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlServerConfig)) return false;

        YamlServerConfig that = (YamlServerConfig) o;

        if (!mpBaseURI.equals(that.mpBaseURI)) return false;
        return resourceName.equals(that.resourceName);
    }

    @Override
    public int hashCode() {
        int result = mpBaseURI.hashCode();
        result = 31 * result + resourceName.hashCode();
        return result;
    }
}
