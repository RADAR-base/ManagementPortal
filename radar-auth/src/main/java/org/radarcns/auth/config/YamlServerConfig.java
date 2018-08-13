package org.radarcns.auth.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarcns.auth.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class YamlServerConfig implements ServerConfig {
    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";
    public static final String CONFIG_FILE_NAME = "radar-is.yml";
    private List<URI> publicKeyEndpoints = new LinkedList<>();
    private String resourceName;
    private List<String> publicKeys = new LinkedList<>();

    private static final Logger log = LoggerFactory.getLogger(YamlServerConfig.class);

    /**
     * Read the configuration from file. This method will first check if the environment variable
     * <code>RADAR_IS_CONFIG_LOCATION</code> is set. If not set, it will look for a file called
     * <code>radar_is.yml</code> on the classpath. The configuration will be kept in a static field,
     * so subsequent calls to this method will return the same object. Use {@link #reloadConfig()}
     * to forcibly reload the configuration from the configuration file.
     * @return The initialized configuration object based on the contents of the configuration file
     * @throws ConfigurationException If there is any problem loading the configuration
     */
    public static synchronized YamlServerConfig readFromFileOrClasspath() {
        String customLocation = System.getenv(LOCATION_ENV);
        URL configFile;
        if (customLocation != null) {
            log.info(LOCATION_ENV + " environment variable set, loading config from {}",
                    customLocation);
            try {
                configFile = new File(customLocation).toURI().toURL();
            } catch (MalformedURLException ex) {
                throw new ConfigurationException(ex);
            }
        } else {
            // if config location not defined, look for it on the classpath
            log.info(LOCATION_ENV
                    + " environment variable not set, looking for it on the classpath");
            configFile = YamlServerConfig.class.getClassLoader().getResource(CONFIG_FILE_NAME);

            if (configFile == null) {
                throw new ConfigurationException("Cannot find " + CONFIG_FILE_NAME
                        + " file in classpath. ");
            }
        }
        log.info("Config file found at {}", configFile.getPath());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream stream = configFile.openStream()) {
            return mapper.readValue(stream, YamlServerConfig.class);
        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        }
    }



    public List<URI> getPublicKeyEndpoints() {
        return publicKeyEndpoints;
    }

    public void setPublicKeyEndpoints(List<URI> publicKeyEndpoints) {
        log.info("Token public key endpoints set to " + publicKeyEndpoints.toString());
        this.publicKeyEndpoints = publicKeyEndpoints;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public List<String> getPublicKeys() {
        return publicKeys;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Set the public keys. This method will detect the public key type (EC or RSA) and parse
     * accordingly.
     * @param publicKeys The public keys to parse
     */
    public void setPublicKeys(List<String> publicKeys) {
        this.publicKeys = publicKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof YamlServerConfig)) {
            return false;
        }
        YamlServerConfig that = (YamlServerConfig) o;
        return Objects.equals(publicKeyEndpoints, that.publicKeyEndpoints)
                && Objects.equals(resourceName, that.resourceName)
                && Objects.equals(publicKeys, that.publicKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKeyEndpoints, resourceName, publicKeys);
    }
}
