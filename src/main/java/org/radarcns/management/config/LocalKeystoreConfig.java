package org.radarcns.management.config;

import java.net.URI;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.radarcns.auth.config.ServerConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

/**
 * Radar-auth server configuration for using a local keystore. This will load the MP public key
 * from our keystore, no need to make an HTTP call to ourselves to fetch it.
 */
public class LocalKeystoreConfig implements ServerConfig {

    public static final String RES_MANAGEMENT_PORTAL = "res_ManagementPortal";
    private final RSAPublicKey publicKey;

    /**
     * Constructor will look for the keystore in the classpath at /config/keystore.jks and load
     * the public key from it.
     */
    public LocalKeystoreConfig() {
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource("/config/keystore.jks"), "radarbase".toCharArray())
                .getKeyPair("selfsigned");
        publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    @Override
    public URI getPublicKeyEndpoint() {
        return null;
    }

    @Override
    public String getResourceName() {
        return RES_MANAGEMENT_PORTAL;
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}
