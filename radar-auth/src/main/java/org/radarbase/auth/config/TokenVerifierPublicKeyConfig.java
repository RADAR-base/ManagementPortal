package org.radarbase.auth.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarbase.auth.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class TokenVerifierPublicKeyConfig implements TokenValidatorConfig {

    private static final Logger log = LoggerFactory.getLogger(TokenVerifierPublicKeyConfig.class);

    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";

    private static final String CONFIG_FILE_NAME = "radar-is.yml";

    private List<URI> publicKeyEndpoints = List.of();

    private String resourceName;

    /**
     * Read the configuration from file. This method will first check if the environment variable
     * <code>RADAR_IS_CONFIG_LOCATION</code> is set. If not set, it will look for a file called
     * <code>radar_is.yml</code> on the classpath. The configuration will be kept in a static field,
     * so subsequent calls to this method will return the same object.
     *
     * @return The initialized configuration object based on the contents of the configuration file
     * @throws ConfigurationException If there is any problem loading the configuration
     */
    public static TokenVerifierPublicKeyConfig readFromFileOrClasspath() {
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
            configFile = Thread.currentThread().getContextClassLoader()
                    .getResource(CONFIG_FILE_NAME);

            if (configFile == null) {
                throw new ConfigurationException(
                        "Cannot find " + CONFIG_FILE_NAME + " file in classpath. ");
            }
        }
        log.info("Config file found at {}", configFile.getPath());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream stream = configFile.openStream()) {
            return mapper.readValue(stream, TokenVerifierPublicKeyConfig.class);
        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
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

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TokenVerifierPublicKeyConfig)) {
            return false;
        }
        TokenVerifierPublicKeyConfig that = (TokenVerifierPublicKeyConfig) o;
        return Objects.equals(publicKeyEndpoints, that.publicKeyEndpoints)
                && Objects.equals(resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKeyEndpoints, resourceName);
    }
}
