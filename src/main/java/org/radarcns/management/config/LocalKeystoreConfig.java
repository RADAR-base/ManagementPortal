package org.radarcns.management.config;

import org.radarcns.auth.config.ServerConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.net.URI;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by dverbeec on 9/10/2017.
 */
public class LocalKeystoreConfig implements ServerConfig {

    RSAPublicKey publicKey;

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
        return "res_ManagementPortal";
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}
