package org.radarcns.management.security.jwt;

import static org.radarcns.management.security.jwt.ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.radarcns.auth.authentication.AlgorithmLoader;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.TokenValidatorConfig;
import org.radarcns.auth.config.TokenVerifierPublicKeyConfig;
import org.radarcns.auth.security.jwk.JavaWebKeySet;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.security.jwt.algorithm.EcdsaJwtAlgorithm;
import org.radarcns.management.security.jwt.algorithm.JwtAlgorithm;
import org.radarcns.management.security.jwt.algorithm.RsaJwtAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Similar to Spring's
 * {@link org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory}. However
 * this class does not assume a specific key type, while the Spring factory assumes RSA keys.
 */
@Component
public class ManagementPortalOauthKeyStoreHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            ManagementPortalOauthKeyStoreHandler.class);

    private final char[] password;

    private final KeyStore store;

    private static final List<Resource> keystorePaths = Arrays.asList(
            new ClassPathResource("/config/keystore.p12"),
            new ClassPathResource("/config/keystore.jks"));

    private Resource loadedResource;

    private final ManagementPortalProperties.Oauth oauthConfig;

    private final List<String> verifierPublicKeyAliasList;

    private String managementPortalBaseUrl;

    private final Boolean enableAdditionalPublicKeyVerifiers;
    
    private TokenValidatorConfig deprecatedValidatedConfig;

    private final List<JWTVerifier> verifiers = new ArrayList<>();

    private final AlgorithmLoader algorithmLoader = new AlgorithmLoader();


    /**
     * Keystore factory. This tries to load the first valid keystore listed in resources.
     *
     * @throws IllegalArgumentException if none of the provided resources can be used to load a
     *                                  keystore.
     */
    private ManagementPortalOauthKeyStoreHandler(
            ManagementPortalProperties managementPortalProperties) {

        validateOauthConfig(managementPortalProperties);

        this.oauthConfig = managementPortalProperties.getOauth();
        this.password = oauthConfig.getKeyStorePassword().toCharArray();
        this.store = loadStore();
        this.verifierPublicKeyAliasList = loadVerifiersPublicKeyAliasList();
        this.enableAdditionalPublicKeyVerifiers =
                managementPortalProperties.getOauth().getEnablePublicKeyVerifiers();
        configureBaseUrl(managementPortalProperties);

        //load verifiers
        loadVerifiersFromAlias();
        loadDeprecatedVerifiers();
    }

    /**
     * Load deprecated verifiers if configured to load.
     */
    private void loadDeprecatedVerifiers() {
        if (enableAdditionalPublicKeyVerifiers) {
            deprecatedValidatedConfig = TokenVerifierPublicKeyConfig.readFromFileOrClasspath();
            if (deprecatedValidatedConfig != null) {
                this.verifiers.addAll(deprecatedValidatedConfig.getPublicKeys()
                        .stream()
                        .map(algorithmLoader::loadDeprecatedAlgorithmFromPublicKey)
                        .filter(Objects::nonNull)
                        .map(algo -> AlgorithmLoader.buildVerifier(algo, RES_MANAGEMENT_PORTAL))
                        .collect(Collectors.toList()));
            }
        }

    }

    private void configureBaseUrl(ManagementPortalProperties managementPortalProperties) {
        String baseUrl = managementPortalProperties.getCommon().getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            // this will be the production url when MP is running
            this.managementPortalBaseUrl = "http://localhost:8080/managementportal";
            logger.warn("managementportal.common.managementPortalBaseUrl is not configured. Using"
                    + " default value {}", this.managementPortalBaseUrl);
        }

        this.managementPortalBaseUrl =
                managementPortalProperties.getCommon().getManagementPortalBaseUrl();
        logger.info("Using Management Portal base-url {}", this.managementPortalBaseUrl);
    }

    /**
     * Creates a {@link ManagementPortalOauthKeyStoreHandler} instance from provided configs.
     * @param managementPortalProperties configs from management portal.
     * @return instance of {@link ManagementPortalOauthKeyStoreHandler}.
     */
    public static ManagementPortalOauthKeyStoreHandler build(
            ManagementPortalProperties managementPortalProperties) {
        return new ManagementPortalOauthKeyStoreHandler(managementPortalProperties);
    }

    private static void validateOauthConfig(ManagementPortalProperties managementPortalProperties) {
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

    private @Nonnull KeyStore loadStore() {
        for (Resource resource : keystorePaths) {
            if (!resource.exists()) {
                logger.debug("JWT key store {} does not exist. Ignoring this resource", resource);
                continue;
            }
            try {
                String fileName = resource.getFilename().toLowerCase(Locale.US);
                String type = fileName.endsWith(".pfx") || fileName.endsWith(".p12")
                        ? "PKCS12" : "jks";
                KeyStore localStore = KeyStore.getInstance(type);
                localStore.load(resource.getInputStream(), this.password);
                logger.debug("Loaded JWT key store {}", resource);
                this.loadedResource = resource;
                return localStore;
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException
                    | IOException ex) {
                logger.error("Cannot load JWT key store {}", ex);
            }
        }
        throw new IllegalArgumentException("Cannot load any of the given JWT key stores "
                + keystorePaths);
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
    public JavaWebKeySet loadJwks() {
        return new JavaWebKeySet(this.verifierPublicKeyAliasList.stream()
                .map(this::getKeyPair)
                .map(ManagementPortalOauthKeyStoreHandler::getJwtAlgorithm)
                .filter(Objects::nonNull)
                .map(JwtAlgorithm::getJwk)
                .collect(Collectors.toList()));
    }

    /**
     * Load default verifiers from configured keystore and aliases.
     */
    private void loadVerifiersFromAlias() {
        List<JWTVerifier> verifiersFromKey = this.verifierPublicKeyAliasList.stream()
                .map(this::getKeyPair)
                .map(ManagementPortalOauthKeyStoreHandler::getJwtAlgorithm)
                .filter(Objects::nonNull)
                .map(JwtAlgorithm::getAlgorithm)
                .filter(Objects::nonNull)
                .map(algo -> AlgorithmLoader.buildVerifier(algo, RES_MANAGEMENT_PORTAL))
                .collect(Collectors.toList());
        this.verifiers.addAll(verifiersFromKey);
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
    private  @Nullable KeyPair getKeyPair(@Nullable String alias, char[] password) {
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

    public TokenValidator getTokenValidator() {
        return new TokenValidator(getKeystoreConfigsForVerifiers());
    }

    private TokenValidatorConfig getKeystoreConfigsForVerifiers() {
        return new TokenValidatorConfig() {

            @Override
            public List<URI> getPublicKeyEndpoints() {
                try {
                    URI managementPortalUrl = new URI(managementPortalBaseUrl + "/oauth/token_key");
                    return Collections.singletonList(managementPortalUrl);
                } catch (URISyntaxException e) {
                    logger.error("Could not create publicKey end point URI");
                    return Collections.emptyList();
                }
            }

            @Override
            public String getResourceName() {
                return RES_MANAGEMENT_PORTAL;
            }

            // management-portal should support old verifiers to verify refresh-tokens, if
            // configured. otherwise, use the token_key endpoint only.
            @Override
            public List<String> getPublicKeys() {
                if (enableAdditionalPublicKeyVerifiers) {
                    return deprecatedValidatedConfig.getPublicKeys();
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }
}
