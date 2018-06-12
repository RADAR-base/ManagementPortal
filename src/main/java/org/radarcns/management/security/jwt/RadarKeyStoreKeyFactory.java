package org.radarcns.management.security.jwt;

import org.springframework.core.io.Resource;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Similar to Spring's
 * {@link org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory}. However
 * this class does not assume a specific key type, while the Spring factory assumes RSA keys.
 */
public class RadarKeyStoreKeyFactory {
    private final Resource resource;

    private final char[] password;

    private KeyStore store;

    private final Object lock = new Object();

    public RadarKeyStoreKeyFactory(Resource resource, char[] password) {
        this.resource = resource;
        this.password = password;
    }

    /**
     * Get a keypair from the store using the store password.
     * @param alias the keypair alias
     * @return the keypair
     */
    public KeyPair getKeyPair(String alias) {
        return getKeyPair(alias, password);
    }

    /**
     * Get a keypair from the store with a given alias and password.
     * @param alias the keypair alias
     * @param password the keypair password
     * @return the keypair
     */
    public KeyPair getKeyPair(String alias, char[] password) {
        try {
            synchronized (lock) {
                if (store == null) {
                    synchronized (lock) {
                        store = KeyStore.getInstance("jks");
                        store.load(resource.getInputStream(), this.password);
                    }
                }
            }
            PrivateKey key = (PrivateKey) store.getKey(alias, password);
            PublicKey publicKey = store.getCertificate(alias).getPublicKey();
            return new KeyPair(publicKey, key);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from store: " + resource, e);
        }
    }
}
