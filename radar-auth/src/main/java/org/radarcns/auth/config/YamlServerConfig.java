package org.radarcns.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class YamlServerConfig implements ServerConfig {
    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";
    public static final String CONFIG_FILE_NAME = "radar-is.yml";
    private URI publicKeyEndpoint;
    private String resourceName;
    private RSAPublicKey publicKey;

    private static YamlServerConfig config;
    private Logger log = LoggerFactory.getLogger(YamlServerConfig.class);

    public YamlServerConfig() {
        log.info("YamlServerConfig initializing...");
    }

    /**
     * Read the configuration from file. This method will first check if the environment variable
     * <code>RADAR_IS_CONFIG_LOCATION</code> is set. If not set, it will look for a file called
     * <code>radar_is.yml</code> on the classpath. The configuration will be kept in a static field,
     * so subsequent calls to this method will return the same object. Use {@link #reloadConfig()}
     * to forcibly reload the configuration from the configuration file.
     * @return The initialized configuration object based on the contents of the configuration file
     * @throws ConfigurationException If there is any problem loading the configuration
     */
    public static YamlServerConfig readFromFileOrClasspath() {
        if (config != null) {
            return config;
        }
        Logger log = LoggerFactory.getLogger(YamlServerConfig.class);
        String customLocation = System.getenv(LOCATION_ENV);
        URL configFile;
        try {
            if (customLocation != null) {
                log.info(LOCATION_ENV + " environment variable set, loading config from {}",
                        customLocation);
                configFile = new File(customLocation).toURI().toURL();
            } else {
                // if config location not defined, look for it on the classpath
                log.info(LOCATION_ENV + " environment variable not set, looking for it on"
                        + " the classpath");
                configFile = YamlServerConfig.class.getClassLoader().getResource(CONFIG_FILE_NAME);
                log.info("Config file found at {}", configFile.getPath());
            }
        } catch (MalformedURLException ex) {
            throw new ConfigurationException(ex);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream stream = configFile.openStream()) {
            return mapper.readValue(stream, YamlServerConfig.class);
        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Forcibly reload the configuration from file, and reinitialize the static field holding the
     * configuration with the new object.
     * @return The new configuration
     * @throws ConfigurationException If there is any problem loading the configuration
     */
    public static YamlServerConfig reloadConfig() {
        config = null;
        return readFromFileOrClasspath();
    }

    public URI getPublicKeyEndpoint() {
        return publicKeyEndpoint;
    }

    public void setPublicKeyEndpoint(URI publicKeyEndpoint) {
        log.info("Token public key endpoint set to " + publicKeyEndpoint.toString());
        this.publicKeyEndpoint = publicKeyEndpoint;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Set the public key. This method converts the public key from a PEM formatted string to a
     * {@link RSAPublicKey} format.
     * @param publicKey The PEM formatted public key
     */
    public void setPublicKey(String publicKey) {
        log.debug("Parsing public key: " + publicKey);
        try (PemReader pemReader = new PemReader(new StringReader(publicKey))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.publicKey = (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof YamlServerConfig)) {
            return false;
        }

        YamlServerConfig that = (YamlServerConfig) other;

        if (!publicKeyEndpoint.equals(that.publicKeyEndpoint)) {
            return false;
        }
        return resourceName.equals(that.resourceName);
    }

    @Override
    public int hashCode() {
        int result = publicKeyEndpoint.hashCode();
        result = 31 * result + resourceName.hashCode();
        return result;
    }
}
