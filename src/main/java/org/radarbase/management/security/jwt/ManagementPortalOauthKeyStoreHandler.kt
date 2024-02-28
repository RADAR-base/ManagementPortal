package org.radarbase.management.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.jwks.JsonWebKeySet
import org.radarbase.auth.jwks.JwkAlgorithmParser
import org.radarbase.auth.jwks.JwksTokenVerifierLoader
import org.radarbase.auth.kratos.KratosTokenVerifierLoader
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.config.ManagementPortalProperties.Oauth
import org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter.Companion.RES_MANAGEMENT_PORTAL
import org.radarbase.management.security.jwt.algorithm.EcdsaJwtAlgorithm
import org.radarbase.management.security.jwt.algorithm.JwtAlgorithm
import org.radarbase.management.security.jwt.algorithm.RsaJwtAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.IllegalArgumentException
import java.security.KeyPair
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.RSAPrivateKey
import java.util.*
import java.util.AbstractMap.SimpleImmutableEntry
import javax.annotation.Nonnull
import javax.servlet.ServletContext
import kotlin.collections.Map.Entry

/**
 * Similar to Spring's
 * [org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory]. However,
 * this class does not assume a specific key type, while the Spring factory assumes RSA keys.
 */
@Component
class ManagementPortalOauthKeyStoreHandler @Autowired constructor(
    environment: Environment, servletContext: ServletContext, private val managementPortalProperties: ManagementPortalProperties
) {
    private val password: CharArray
    private val store: KeyStore
    private val loadedResource: Resource
    private val oauthConfig: Oauth
    private val verifierPublicKeyAliasList: List<String>
    private val managementPortalBaseUrl: String
    val verifiers: MutableList<JWTVerifier>
    val refreshTokenVerifiers: MutableList<JWTVerifier>

    /**
     * Keystore factory. This tries to load the first valid keystore listed in resources.
     *
     * @throws IllegalArgumentException if none of the provided resources can be used to load a
     * keystore.
     */
    init {
        checkOAuthConfig(managementPortalProperties)
        oauthConfig = managementPortalProperties.oauth
        password = oauthConfig.keyStorePassword.toCharArray()
        val loadedStore: Entry<Resource, KeyStore> = loadStore()
        loadedResource = loadedStore.key
        store = loadedStore.value
        verifierPublicKeyAliasList = loadVerifiersPublicKeyAliasList()
        managementPortalBaseUrl =
            ("http://localhost:" + environment.getProperty("server.port") + servletContext.contextPath)
        logger.info("Using Management Portal base-url {}", managementPortalBaseUrl)
        val algorithms = loadAlgorithmsFromAlias().filter { obj: Algorithm? -> Objects.nonNull(obj) }.toList()
        verifiers = algorithms.map { algo: Algorithm? ->
            JWT.require(algo).withAudience(ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL).build()
        }.toMutableList()
        // No need to check audience with a refresh token: it can be used
        // to refresh tokens intended for other resources.
        refreshTokenVerifiers = algorithms.map { algo: Algorithm -> JWT.require(algo).build() }.toMutableList()
    }

    @Nonnull
    private fun loadStore(): Entry<Resource, KeyStore> {
        for (resource in KEYSTORE_PATHS) {
            if (!resource.exists()) {
                logger.debug("JWT key store {} does not exist. Ignoring this resource", resource)
                continue
            }
            try {
                val fileName = Objects.requireNonNull(resource.filename).lowercase()
                val type = if (fileName.endsWith(".pfx") || fileName.endsWith(".p12")) "PKCS12" else "jks"
                val localStore = KeyStore.getInstance(type)
                localStore.load(resource.inputStream, password)
                logger.debug("Loaded JWT key store {}", resource)
                if (localStore != null)
                    return SimpleImmutableEntry(resource, localStore)
            } catch (ex: CertificateException) {
                logger.error("Cannot load JWT key store", ex)
            } catch (ex: NoSuchAlgorithmException) {
                logger.error("Cannot load JWT key store", ex)
            } catch (ex: KeyStoreException) {
                logger.error("Cannot load JWT key store", ex)
            } catch (ex: IOException) {
                logger.error("Cannot load JWT key store", ex)
            }
        }
        throw IllegalArgumentException(
            "Cannot load any of the given JWT key stores " + KEYSTORE_PATHS
        )
    }

    private fun loadVerifiersPublicKeyAliasList(): List<String> {
        val publicKeyAliases: MutableList<String> = ArrayList()
        oauthConfig.signingKeyAlias?.let { publicKeyAliases.add(it) }
        if (oauthConfig.checkingKeyAliases != null) {
            publicKeyAliases.addAll(oauthConfig.checkingKeyAliases!!)
        }
        return publicKeyAliases
    }

    /**
     * Returns configured public keys of token verifiers.
     * @return List of public keys for token verification.
     */
    fun loadJwks(): JsonWebKeySet {
        return JsonWebKeySet(verifierPublicKeyAliasList.map { alias: String -> this.getKeyPair(alias) }
            .map { keyPair: KeyPair? -> getJwtAlgorithm(keyPair) }.mapNotNull { obj: JwtAlgorithm? -> obj?.jwk })
    }

    /**
     * Load default verifiers from configured keystore and aliases.
     */
    private fun loadAlgorithmsFromAlias(): Collection<Algorithm> {
        return verifierPublicKeyAliasList
            .map { alias: String -> this.getKeyPair(alias) }
            .mapNotNull { keyPair -> getJwtAlgorithm(keyPair) }
            .map { obj: JwtAlgorithm -> obj.algorithm }
    }

    val algorithmForSigning: Algorithm
        /**
         * Returns the signing algorithm extracted based on signing alias configured from keystore.
         * @return signing algorithm.
         */
        get() {
            val signKey = oauthConfig.signingKeyAlias
            logger.debug("Using JWT signing key {}", signKey)
            val keyPair = getKeyPair(signKey) ?: throw IllegalArgumentException(
                "Cannot load JWT signing key " + signKey + " from JWT key store."
            )
            return getAlgorithmFromKeyPair(keyPair)
        }

    /**
     * Get a key pair from the store using the store password.
     * @param alias key pair alias
     * @return loaded key pair or `null` if the key store does not contain a loadable key with
     * given alias.
     * @throws IllegalArgumentException if the key alias password is wrong or the key cannot
     * loaded.
     */
    private fun getKeyPair(alias: String): KeyPair? {
        return getKeyPair(alias, password)
    }

    /**
     * Get a key pair from the store with a given alias and password.
     * @param alias key pair alias
     * @param password key pair password
     * @return loaded key pair or `null` if the key store does not contain a loadable key with
     * given alias.
     * @throws IllegalArgumentException if the key alias password is wrong or the key cannot
     * load.
     */
    private fun getKeyPair(alias: String, password: CharArray): KeyPair? {
        return try {
            val key = store.getKey(alias, password) as PrivateKey?
            if (key == null) {
                logger.warn(
                    "JWT key store {} does not contain private key pair for alias {}", loadedResource, alias
                )
                return null
            }
            val cert = store.getCertificate(alias)
            if (cert == null) {
                logger.warn(
                    "JWT key store {} does not contain certificate pair for alias {}", loadedResource, alias
                )
                return null
            }
            val publicKey = cert.publicKey
            if (publicKey == null) {
                logger.warn(
                    "JWT key store {} does not contain public key pair for alias {}", loadedResource, alias
                )
                return null
            }
            KeyPair(publicKey, key)
        } catch (ex: NoSuchAlgorithmException) {
            logger.warn(
                "JWT key store {} contains unknown algorithm for key pair with alias {}: {}",
                loadedResource,
                alias,
                ex.toString()
            )
            null
        } catch (ex: UnrecoverableKeyException) {
            throw IllegalArgumentException(
                "JWT key store $loadedResource contains unrecoverable key pair with alias $alias (the password may be wrong)",
                ex
            )
        } catch (ex: KeyStoreException) {
            throw IllegalArgumentException(
                "JWT key store $loadedResource contains unrecoverable key pair with alias $alias (the password may be wrong)",
                ex
            )
        }
    }

    val tokenValidator: TokenValidator
        /** Get the default token validator.  */
        get() {
            val loaderList = listOf(
                JwksTokenVerifierLoader(
                    managementPortalBaseUrl + "/oauth/token_key",
                    RES_MANAGEMENT_PORTAL,
                    JwkAlgorithmParser()
                ),
                KratosTokenVerifierLoader(managementPortalProperties.identityServer.publicUrl(), requireAal2 = managementPortalProperties.oauth.requireAal2),
            )
            return TokenValidator(loaderList)
        }

    companion object {
        private val logger = LoggerFactory.getLogger(
            ManagementPortalOauthKeyStoreHandler::class.java
        )
        private val KEYSTORE_PATHS = listOf<Resource>(
            ClassPathResource("/config/keystore.p12"), ClassPathResource("/config/keystore.jks")
        )

        private fun checkOAuthConfig(managementPortalProperties: ManagementPortalProperties) {
            val oauthConfig = managementPortalProperties.oauth
            if (oauthConfig.keyStorePassword.isEmpty()) {
                logger.error("oauth.keyStorePassword is empty")
                throw IllegalArgumentException("oauth.keyStorePassword is empty")
            }
            if (oauthConfig.signingKeyAlias == null || oauthConfig.signingKeyAlias!!.isEmpty()) {
                logger.error("oauth.signingKeyAlias is empty")
                throw IllegalArgumentException("OauthConfig is not provided")
            }
        }

        /**
         * Returns extracted [Algorithm] from the KeyPair.
         * @param keyPair to find algorithm.
         * @return extracted algorithm.
         */
        private fun getAlgorithmFromKeyPair(keyPair: KeyPair): Algorithm {
            val alg = getJwtAlgorithm(keyPair) ?: throw IllegalArgumentException(
                "KeyPair type " + keyPair.private.algorithm + " is unknown."
            )
            return alg.algorithm
        }

        /**
         * Get the JWT algorithm to sign or verify JWTs with.
         * @param keyPair key pair for signing/verifying.
         * @return algorithm or `null` if the key type is unknown.
         */
        private fun getJwtAlgorithm(keyPair: KeyPair?): JwtAlgorithm? {
            if (keyPair == null) {
                return null
            }
            val privateKey = keyPair.private
            return when (privateKey) {
                is ECPrivateKey -> {
                    EcdsaJwtAlgorithm(keyPair)
                }

                is RSAPrivateKey -> {
                    RsaJwtAlgorithm(keyPair)
                }

                else -> {
                    logger.warn(
                        "No JWT algorithm found for key type {}", privateKey.javaClass
                    )
                    null
                }
            }
        }
    }
}
