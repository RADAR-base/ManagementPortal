package org.radarbase.auth.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.radarbase.auth.authorization.Permission.Companion.scopes
import org.radarbase.auth.jwks.JwksTokenVerifierLoader.Companion.toTokenVerifier
import org.radarbase.management.domain.Authority
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.JwtAuthenticationFilter
import org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter
import org.slf4j.LoggerFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.test.web.servlet.request.RequestPostProcessor
import java.security.KeyStore
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

/**
 * Created by dverbeec on 29/06/2017.
 */
object OAuthHelper {
    private val logger = LoggerFactory.getLogger(OAuthHelper::class.java)
    private var validEcToken: String? = null
    private var validRsaToken: String? = null
    const val TEST_KEYSTORE_PASSWORD = "radarbase"
    const val TEST_SIGNKEY_ALIAS = "radarbase-managementportal-ec"
    const val TEST_CHECKKEY_ALIAS = "radarbase-managementportal-rsa"
    val SCOPES = scopes()
    val AUTHORITIES = arrayOf("ROLE_SYS_ADMIN")
    val ROLES = arrayOf("ROLE_SYS_ADMIN")
    val SOURCES = arrayOf<String>()
    val AUD = arrayOf(ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL)
    const val CLIENT = "unit_test"
    const val USER = "admin"
    const val ISS = "RADAR"
    const val JTI = "some-jwt-id"
    private var verifiers: List<TokenVerifierLoader>? = null

    init {
        try {
            setUp()
        } catch (e: Exception) {
            logger.error("Failed to set up OAuthHelper", e)
        }
    }

    /**
     * Create a request post processor that adds a valid bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    fun bearerToken(): RequestPostProcessor {
        return RequestPostProcessor { mockRequest: MockHttpServletRequest ->
            mockRequest.addHeader("Authorization", "Bearer " + validEcToken)
            mockRequest
        }
    }

    /**
     * Create a request post processor that adds a valid RSA bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    fun rsaBearerToken(): RequestPostProcessor {
        return RequestPostProcessor { mockRequest: MockHttpServletRequest ->
            mockRequest.addHeader("Authorization", "Bearer " + validRsaToken)
            mockRequest
        }
    }

    /**
     * Set up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
     * @throws Exception If anything goes wrong during setup
     */
    @Throws(Exception::class)
    fun setUp() {
        val ks = KeyStore.getInstance("PKCS12")
        Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("config/keystore.p12").use { keyStream ->
                checkNotNull(keyStream) { "Cannot find keystore to set up OAuth" }
                ks.load(keyStream, TEST_KEYSTORE_PASSWORD.toCharArray())

                // get the EC keypair for signing
                val privateKey = ks.getKey(
                    TEST_SIGNKEY_ALIAS,
                    TEST_KEYSTORE_PASSWORD.toCharArray()
                ) as ECPrivateKey
                val cert = ks.getCertificate(TEST_SIGNKEY_ALIAS)
                val publicKey = cert.publicKey as ECPublicKey
                val ecdsa = Algorithm.ECDSA256(publicKey, privateKey)
                validEcToken = createValidToken(ecdsa)

                // also get an RSA keypair to test accepting multiple keys
                val rsaPrivateKey = ks.getKey(
                    TEST_CHECKKEY_ALIAS,
                    TEST_KEYSTORE_PASSWORD.toCharArray()
                ) as RSAPrivateKey
                val rsaPublicKey = ks.getCertificate(TEST_CHECKKEY_ALIAS)
                    .publicKey as RSAPublicKey
                val rsa = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey)
                validRsaToken = createValidToken(rsa)
                val verifierList = listOf(ecdsa, rsa)
                    .map { alg: Algorithm? ->
                        alg?.toTokenVerifier(ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL)
                    }
                    .requireNoNulls()
                    .toList()
                verifiers = listOf(StaticTokenVerifierLoader(verifierList))
            }
    }

    /**
     * Helper method to initialize an authentication filter for use in test classes.
     *
     * @return an initialized JwtAuthenticationFilter
     */
    fun createAuthenticationFilter(): JwtAuthenticationFilter {
        val userRepository = Mockito.mock(UserRepository::class.java)
        Mockito.`when`(userRepository.findOneByLogin(ArgumentMatchers.anyString())).thenReturn(
            createAdminUser()
        )
        return JwtAuthenticationFilter(createTokenValidator(), { auth: Authentication? -> auth })
    }

    /**
     * Helper method to initialize a token validator for use in test classes.
     *
     * @return configured TokenValidator
     */
    private fun createTokenValidator(): TokenValidator {
        // Use tokenValidator with known JWTVerifier which signs.
        return TokenValidator(verifiers)
    }

    private fun createAdminUser(): User {
        val user = User()
        user.id = 1L
        user.setLogin("admin")
        user.activated = true
        user.roles = mutableSetOf(Role(Authority("ROLE_SYS_ADMIN")))

        return user
    }

    private fun createValidToken(algorithm: Algorithm): String {
        val exp = Instant.now().plusSeconds((30 * 60).toLong())
        val iat = Instant.now()
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(Date.from(iat))
            .withExpiresAt(Date.from(exp))
            .withAudience("res_ManagementPortal")
            .withSubject(USER)
            .withArrayClaim("scope", SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withArrayClaim("aud", AUD)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
    }
}
