package org.radarcns.management.security.jwt;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;

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
     * @throws IllegalStateException if the keys cannot be loaded, e.g. the alias does not exist,
     *                               the configured password is invalid or the store file cannot be
     *                               loaded.
     */
    public KeyPair getKeyPair(String alias) {
        return getKeyPair(alias, password);
    }

    /**
     * Get a keypair from the store with a given alias and password.
     * @param alias the keypair alias
     * @param password the keypair password
     * @return the keypair
     * @throws IllegalStateException if the keys cannot be loaded, e.g. the alias does not exist,
     *                               the password is invalid or the store file cannot be loaded.
     */
    public KeyPair getKeyPair(String alias, char[] password) {
        try {
            KeyStore localStore;
            synchronized (lock) {
                if (store == null) {
                    store = KeyStore.getInstance("jks");
                    store.load(resource.getInputStream(), this.password);
                }
                localStore = store;
            }
            PrivateKey key = (PrivateKey) localStore.getKey(alias, password);
            PublicKey publicKey = localStore.getCertificate(alias).getPublicKey();
            return new KeyPair(publicKey, key);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from store: " + resource, e);
        }
    }

    /**
     * Stream JwtAlgorithm for given keystore key pair aliases.
     * Unknown algorithms will be excluded from output.
     * @param aliases key aliases, possibly null.
     * @return stream of JwtAlgorithm objects matching given aliases.
     */
    public Stream<JwtAlgorithm> streamJwtAlgorithm(Collection<String> aliases) {
        if (aliases == null) {
            return Stream.empty();
        }
        return aliases.stream()
                .map(this::getKeyPair)
                .map(RadarJwtAccessTokenConverter::getJwtAlgorithm)
                .filter(Objects::nonNull);
    }
}
