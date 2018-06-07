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
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class YamlServerConfig implements ServerConfig {
    public static final String LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION";
    public static final String CONFIG_FILE_NAME = "radar-is.yml";
    private List<URI> publicKeyEndpoints = new LinkedList<>();
    private String resourceName;
    private List<PublicKey> publicKeys = new LinkedList<>();

    private static YamlServerConfig config;
    private final Logger log = LoggerFactory.getLogger(YamlServerConfig.class);

    // a map with as key the string to search for in a PEM encoded public key, and as value the
    // KeyFactory type to request
    private final Map<String, String> keyFactoryTypes = new HashMap<>();

    /**
     * Default constructor. Initializes the keyFactoryTypes map.
     */
    public YamlServerConfig() {
        keyFactoryTypes.put("-----BEGIN PUBLIC KEY-----", "RSA");
        keyFactoryTypes.put("-----BEGIN EC PUBLIC KEY-----", "EC");
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
    public List<PublicKey> getPublicKeys() {
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
        this.publicKeys = publicKeys.stream().map(this::parseKey).collect(Collectors.toList());
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

    private PublicKey parseKey(String publicKey) {
        String factoryType = keyFactoryTypes.keySet().stream()
                // find the string that is contained in publicKey
                .filter(publicKey::contains)
                .findFirst()
                // get the actual factory type
                .map(keyFactoryTypes::get)
                // if not found throw a ConfigurationException
                .orElseThrow(() -> new ConfigurationException("Unsupported public key: "
                        + publicKey));
        log.debug("Parsing {} public key: {}", factoryType, publicKey);
        try (PemReader pemReader = new PemReader(new StringReader(publicKey))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(factoryType);
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }
}
