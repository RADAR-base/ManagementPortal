package org.radarbase.management.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.radarbase.auth.authentication.TokenValidator;
import org.radarbase.auth.jwks.JsonWebKeySet;
import org.radarbase.auth.jwks.JwkAlgorithmParser;
import org.radarbase.auth.jwks.JwksTokenVerifierLoader;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.security.jwt.algorithm.EcdsaJwtAlgorithm;
import org.radarbase.management.security.jwt.algorithm.JwtAlgorithm;
import org.radarbase.management.security.jwt.algorithm.RsaJwtAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL;

/**
 * Similar to Spring's
 * {@link org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory}. However,
 * this class does not assume a specific key type, while the Spring factory assumes RSA keys.
 */
@Component
public class ManagementPortalOauthKeyStoreHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            ManagementPortalOauthKeyStoreHandler.class);

    private static final List<Resource> KEYSTORE_PATHS = Arrays.asList(
            new ClassPathResource("/config/keystore.p12"),
            new ClassPathResource("/config/keystore.jks"));

    private final char[] password;

    private final KeyStore store;

    private final Resource loadedResource;

    private final ManagementPortalProperties.Oauth oauthConfig;

    private final List<String> verifierPublicKeyAliasList;

    private final String managementPortalBaseUrl;

    private final List<JWTVerifier> verifiers;
    private final List<JWTVerifier> refreshTokenVerifiers;

    /**
     * Keystore factory. This tries to load the first valid keystore listed in resources.
     *
     * @throws IllegalArgumentException if none of the provided resources can be used to load a
     *                                  keystore.
     */
    @Autowired
    public ManagementPortalOauthKeyStoreHandler(
            Environment environment,
            ServletContext servletContext,
            ManagementPortalProperties managementPortalProperties) {

        checkOAuthConfig(managementPortalProperties);

        this.oauthConfig = managementPortalProperties.getOauth();
        this.password = oauthConfig.getKeyStorePassword().toCharArray();
        Map.Entry<Resource, KeyStore> loadedStore = loadStore();
        this.loadedResource = loadedStore.getKey();
        this.store = loadedStore.getValue();
        this.verifierPublicKeyAliasList = loadVerifiersPublicKeyAliasList();

        this.managementPortalBaseUrl = "http://localhost:"
                + environment.getProperty("server.port")
                + servletContext.getContextPath();
        logger.info("Using Management Portal base-url {}", this.managementPortalBaseUrl);

        List<Algorithm> algorithms = loadAlgorithmsFromAlias()
                .filter(Objects::nonNull)
                .toList();

        verifiers = algorithms.stream()
                .map(algo -> JWT.require(algo).withAudience(RES_MANAGEMENT_PORTAL).build())
                .toList();
        // No need to check audience with a refresh token: it can be used
        // to refresh tokens intended for other resources.
        refreshTokenVerifiers = algorithms.stream()
                .map(algo -> JWT.require(algo).build())
                .toList();
    }

    private static void checkOAuthConfig(ManagementPortalProperties managementPortalProperties) {
        ManagementPortalProperties.Oauth oauthConfig = managementPortalProperties.getOauth();

        if ( oauthConfig == null ) {
            logger.error("Could not find valid Oauth Config. Please configure compulsary "
                    + "properties of Oauth configs of Management Portal");
            throw new IllegalArgumentException("OauthConfig is not provided");
        }

        if (oauthConfig.getKeyStorePassword() == null || oauthConfig.getKeyStorePassword()
                .isEmpty()) {
            logger.error("oauth.keyStorePassword is empty");
            throw new IllegalArgumentException("oauth.keyStorePassword is empty");
        }

        if (oauthConfig.getSigningKeyAlias() == null || oauthConfig.getSigningKeyAlias()
                .isEmpty()) {
            logger.error("oauth.signingKeyAlias is empty");
            throw new IllegalArgumentException("OauthConfig is not provided");
        }

    }

    @Nonnull
    private Map.Entry<Resource, KeyStore> loadStore() {
        for (Resource resource : KEYSTORE_PATHS) {
            if (!resource.exists()) {
                logger.debug("JWT key store {} does not exist. Ignoring this resource", resource);
                continue;
            }
            try {
                String fileName = requireNonNull(resource.getFilename()).toLowerCase(Locale.ROOT);
                String type = fileName.endsWith(".pfx") || fileName.endsWith(".p12")
                        ? "PKCS12" : "jks";
                KeyStore localStore = KeyStore.getInstance(type);
                localStore.load(resource.getInputStream(), this.password);
                logger.debug("Loaded JWT key store {}", resource);
                return new AbstractMap.SimpleImmutableEntry<>(resource, localStore);
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException
                    | IOException ex) {
                logger.error("Cannot load JWT key store", ex);
            }
        }
        throw new IllegalArgumentException("Cannot load any of the given JWT key stores "
                + KEYSTORE_PATHS);
    }

    private List<String> loadVerifiersPublicKeyAliasList() {
        List<String> publicKeyAliases = new ArrayList<>();
        publicKeyAliases.add(oauthConfig.getSigningKeyAlias());
        if (oauthConfig.getCheckingKeyAliases() != null) {
            publicKeyAliases.addAll(oauthConfig.getCheckingKeyAliases());
        }
        return publicKeyAliases;
    }

    /**
     * Returns configured public keys of token verifiers.
     * @return List of public keys for token verification.
     */
    public JsonWebKeySet loadJwks() {
        return new JsonWebKeySet(this.verifierPublicKeyAliasList.stream()
                .map(this::getKeyPair)
                .map(ManagementPortalOauthKeyStoreHandler::getJwtAlgorithm)
                .filter(Objects::nonNull)
                .map(JwtAlgorithm::getJwk)
                .toList());
    }

    /**
     * Load default verifiers from configured keystore and aliases.
     */
    private Stream<Algorithm> loadAlgorithmsFromAlias() {
        return this.verifierPublicKeyAliasList.stream()
                .map(this::getKeyPair)
                .map(ManagementPortalOauthKeyStoreHandler::getJwtAlgorithm)
                .filter(Objects::nonNull)
                .map(JwtAlgorithm::getAlgorithm);
    }

    public List<JWTVerifier> getVerifiers() {
        return this.verifiers;
    }


    /**
     * Returns the signing algorithm extracted based on signing alias configured from keystore.
     * @return signing algorithm.
     */
    public Algorithm getAlgorithmForSigning() {
        String signKey = oauthConfig.getSigningKeyAlias();
        logger.debug("Using JWT signing key {}", signKey);
        KeyPair keyPair = getKeyPair(signKey);
        if (keyPair == null) {
            throw new IllegalArgumentException("Cannot load JWT signing key " + signKey
                    + " from JWT key store.");
        }

        return getAlgorithmFromKeyPair(keyPair);
    }

    /**
     * Get a key pair from the store using the store password.
     * @param alias key pair alias
     * @return loaded key pair or {@code null} if the key store does not contain a loadable key with
     *         given alias.
     * @throws IllegalArgumentException if the key alias password is wrong or the key cannot
     *                                  loaded.
     */
    private  @Nullable KeyPair getKeyPair(@Nullable String alias) {
        return getKeyPair(alias, password);
    }

    /**
     * Get a key pair from the store with a given alias and password.
     * @param alias key pair alias
     * @param password key pair password
     * @return loaded key pair or {@code null} if the key store does not contain a loadable key with
     *         given alias.
     * @throws IllegalArgumentException if the key alias password is wrong or the key cannot
     *                                  loaded.
     */
    private @Nullable KeyPair getKeyPair(@Nullable String alias, char[] password) {
        try {
            PrivateKey key = (PrivateKey) store.getKey(alias, password);
            if (key == null) {
                logger.warn("JWT key store {} does not contain private key pair for alias {}",
                        loadedResource, alias);
                return null;
            }
            Certificate cert = store.getCertificate(alias);
            if (cert == null) {
                logger.warn("JWT key store {} does not contain certificate pair for alias {}",
                        loadedResource, alias);
                return null;
            }
            PublicKey publicKey = cert.getPublicKey();
            if (publicKey == null) {
                logger.warn("JWT key store {} does not contain public key pair for alias {}",
                        loadedResource, alias);
                return null;
            }

            return new KeyPair(publicKey, key);
        } catch (NoSuchAlgorithmException ex) {
            logger.warn(
                    "JWT key store {} contains unknown algorithm for key pair with alias {}: {}",
                    loadedResource, alias, ex.toString());
            return null;
        } catch (UnrecoverableKeyException | KeyStoreException ex) {
            throw new IllegalArgumentException("JWT key store " + loadedResource
                    + " contains unrecoverable key pair with alias "
                    + alias + " (the password may be wrong)", ex);
        }
    }

    /**
     * Returns extracted {@link Algorithm} from the KeyPair.
     * @param keyPair to find algorithm.
     * @return extracted algorithm.
     */
    private static Algorithm getAlgorithmFromKeyPair(KeyPair keyPair) {
        JwtAlgorithm alg = getJwtAlgorithm(keyPair);
        if (alg == null) {
            throw new IllegalArgumentException("KeyPair type "
                    + keyPair.getPrivate().getAlgorithm() + " is unknown.");
        }
        return  alg.getAlgorithm();
    }

    /**
     * Get the JWT algorithm to sign or verify JWTs with.
     * @param keyPair key pair for signing/verifying.
     * @return algorithm or {@code null} if the key type is unknown.
     */
    private static @Nullable JwtAlgorithm getJwtAlgorithm(@Nullable KeyPair keyPair) {

        if (keyPair == null) {
            return null;
        }
        PrivateKey privateKey = keyPair.getPrivate();

        if (privateKey instanceof ECPrivateKey) {
            return new EcdsaJwtAlgorithm(keyPair);
        } else if (privateKey instanceof RSAPrivateKey) {
            return new RsaJwtAlgorithm(keyPair);
        } else {
            logger.warn("No JWT algorithm found for key type {}", privateKey.getClass());
            return null;
        }
    }

    /** Get the default token validator. */
    public TokenValidator getTokenValidator() {
        var jwksLoader = new JwksTokenVerifierLoader(
                managementPortalBaseUrl + "/oauth/token_key",
                "res_ManagementPortal",
                new JwkAlgorithmParser()
        );
        return new TokenValidator(List.of(jwksLoader));
    }

    public List<JWTVerifier> getRefreshTokenVerifiers() {
        return refreshTokenVerifiers;
    }
}
