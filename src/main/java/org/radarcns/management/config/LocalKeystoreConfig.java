package org.radarcns.management.config;

import static org.radarcns.management.security.jwt.RadarJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.management.security.jwt.JwtAlgorithm;
import org.radarcns.management.security.jwt.KeyStoreUtil;
import org.radarcns.management.security.jwt.RadarKeyStoreKeyFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Radar-auth server configuration for using a local keystore. This will load the MP public key
 * from our keystore, no need to make an HTTP call to ourselves to fetch it.
 */
public class LocalKeystoreConfig implements ServerConfig {


    private final List<String> publicKeys;

    /**
     * Constructor will look for the keystore in the classpath at /config/keystore.jks and load
     * the public key from it.
     */
    public LocalKeystoreConfig(String keyStorePassword, List<String> checkingKeyAliases) {
        RadarKeyStoreKeyFactory keyFactory = new RadarKeyStoreKeyFactory(
                Arrays.asList(
                        new ClassPathResource("/config/keystore.p12"),
                        new ClassPathResource("/config/keystore.jks")),
                keyStorePassword.toCharArray());
        // Load the key and convert to PEM format, internally spring uses the
        // JwtAccessTokenConverter to do that for the token_key endpoint. We can use it here as
        // well.
        publicKeys = checkingKeyAliases.stream()
                .map(keyFactory::getKeyPair)
                .map(KeyStoreUtil::getJwtAlgorithm)
                .filter(Objects::nonNull)
                .map(JwtAlgorithm::getVerifierKeyEncodedString)
                .collect(Collectors.toList());
    }

    @Override
    public List<URI> getPublicKeyEndpoints() {
        return Collections.emptyList();
    }

    @Override
    public String getResourceName() {
        return RES_MANAGEMENT_PORTAL;
    }

    @Override
    public List<String> getPublicKeys() {
        return publicKeys;
    }
}
