package org.radarbase.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import org.radarbase.auth.authorization.Permission
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Sets up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
 */
object TokenTestUtils {
    private val logger = LoggerFactory.getLogger(TokenTestUtils::class.java)

    const val PUBLIC_KEY_PATH = "/oauth/token_key"
    val PUBLIC_KEY_STRING: String
    @JvmField val PUBLIC_KEY_BODY: String
    @JvmField val VALID_RSA_TOKEN: String
    @JvmField val INCORRECT_AUDIENCE_TOKEN: String
    @JvmField val EXPIRED_TOKEN: String
    @JvmField val INCORRECT_ALGORITHM_TOKEN: String
    @JvmField val SCOPE_TOKEN: DecodedJWT
    @JvmField val ORGANIZATION_ADMIN_TOKEN: DecodedJWT
    @JvmField val PROJECT_ADMIN_TOKEN: DecodedJWT
    @JvmField val SUPER_USER_TOKEN: DecodedJWT
    @JvmField val MULTIPLE_ROLES_IN_PROJECT_TOKEN: DecodedJWT
    val AUTHORITIES = arrayOf("ROLE_SYS_ADMIN", "ROLE_USER")
    val ALL_SCOPES = Permission.scopes()
    val ROLES = arrayOf(
        "PROJECT1:ROLE_PROJECT_ADMIN",
        "PROJECT2:ROLE_PARTICIPANT"
    )
    val SOURCES = arrayOf<String>()
    const val CLIENT = "unit_test"
    const val USER = "admin"
    const val ISS = "RADAR"
    const val JTI = "some-jwt-id"
    const val APPLICATION_JSON = "application/json"
    const val WIREMOCK_PORT = 8089

    init {
        val provider: RSAKeyProvider = try {
            loadKeys()
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("Failed to load keys for test", e)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to load keys for test", e)
        }
        val publicKey = provider.getPublicKeyById("selfsigned")
        PUBLIC_KEY_STRING = String(
            Base64.getEncoder().encode(
                provider.getPublicKeyById("selfsigned").encoded
            )
        )
        val algorithm = Algorithm.RSA256(publicKey, provider.privateKey)
        PUBLIC_KEY_BODY = """
            {
              "keys" : [ {
                "alg" : "${algorithm.name}",
                "kty" : "RSA",
                "value" : "-----BEGIN PUBLIC KEY-----\n$PUBLIC_KEY_STRING\n-----END PUBLIC KEY-----"
              } ]
            }
            """.trimIndent()
        logger.info("Key body {}", PUBLIC_KEY_BODY)
        val exp = Instant.now() + Duration.ofMinutes(30)
        val iat = Instant.now()
        VALID_RSA_TOKEN = initValidToken(algorithm, exp, iat)
        SUPER_USER_TOKEN = JWT.decode(VALID_RSA_TOKEN)
        PROJECT_ADMIN_TOKEN = initProjectAdminToken(algorithm, exp, iat)
        ORGANIZATION_ADMIN_TOKEN = initOrgananizationAdminToken(algorithm, exp, iat)
        MULTIPLE_ROLES_IN_PROJECT_TOKEN = initMultipleRolesToken(algorithm, exp, iat)
        INCORRECT_AUDIENCE_TOKEN = initIncorrectAudienceToken(algorithm, exp, iat)
        SCOPE_TOKEN = initTokenWithScopes(algorithm, exp, iat)
        INCORRECT_ALGORITHM_TOKEN = initIncorrectAlgorithmToken(exp, iat)
        val past = Instant.now().minusSeconds(1)
        val iatpast = Instant.now().minusSeconds((30 * 60 + 1).toLong())
        EXPIRED_TOKEN = initExpiredToken(algorithm, past, iatpast)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun loadKeys(): RSAKeyProvider {
        val ks = KeyStore.getInstance("PKCS12")
        Thread.currentThread().contextClassLoader
            .getResourceAsStream("keystore.p12")
            .use { keyStream -> ks.load(keyStream, "radarbase".toCharArray()) }
        val privateKey = ks.getKey(
            "selfsigned",
            "radarbase".toCharArray()
        ) as RSAPrivateKey
        val cert = ks.getCertificate("selfsigned")
        return object : RSAKeyProvider {
            override fun getPublicKeyById(keyId: String): RSAPublicKey =
                cert.publicKey as RSAPublicKey

            override fun getPrivateKey(): RSAPrivateKey = privateKey

            override fun getPrivateKeyId(): String = "1"
        }
    }

    private fun initExpiredToken(algorithm: Algorithm, past: Instant, iatpast: Instant): String {
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iatpast)
            .withExpiresAt(past)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
    }

    private fun initIncorrectAlgorithmToken(exp: Instant, iat: Instant): String {
        val psk = Algorithm.HMAC256("super-secret-stuff")
        // token signed with a pre-shared key
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", arrayOf("ROLE_PROJECT_ADMIN"))
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", arrayOf<String>())
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(psk)
    }

    private fun initIncorrectAudienceToken(
        algorithm: Algorithm, exp: Instant,
        iat: Instant
    ): String {
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience("SOME_AUDIENCE")
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", arrayOf("ROLE_PROJECT_ADMIN"))
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", arrayOf<String>())
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
    }

    private fun initMultipleRolesToken(
        algorithm: Algorithm, exp: Instant,
        iat: Instant
    ): DecodedJWT {
        val multipleRolesInProjectToken = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", arrayOf("ROLE_PROJECT_ADMIN"))
            .withArrayClaim(
                "roles", arrayOf(
                    "PROJECT2:ROLE_PROJECT_ADMIN",
                    "PROJECT2:ROLE_PARTICIPANT"
                )
            )
            .withArrayClaim("sources", arrayOf("source-1"))
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
        return JWT.decode(multipleRolesInProjectToken)
    }

    private fun initOrgananizationAdminToken(
        algorithm: Algorithm, exp: Instant,
        iat: Instant
    ): DecodedJWT {
        val projectAdminToken = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", arrayOf("ROLE_ORGANIZATION_ADMIN"))
            .withArrayClaim("roles", arrayOf("main:ROLE_ORGANIZATION_ADMIN"))
            .withArrayClaim("sources", arrayOf<String>())
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
        return JWT.decode(projectAdminToken)
    }

    private fun initProjectAdminToken(
        algorithm: Algorithm,
        exp: Instant,
        iat: Instant
    ): DecodedJWT {
        val projectAdminToken = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim(
                "authorities", arrayOf(
                    "ROLE_PROJECT_ADMIN",
                    "ROLE_PARTICIPANT"
                )
            )
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", arrayOf<String>())
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
        return JWT.decode(projectAdminToken)
    }

    private fun initValidToken(algorithm: Algorithm, exp: Instant, iat: Instant): String {
        return JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject(USER)
            .withArrayClaim("scope", ALL_SCOPES)
            .withArrayClaim("authorities", AUTHORITIES)
            .withArrayClaim("roles", ROLES)
            .withArrayClaim("sources", SOURCES)
            .withClaim("client_id", CLIENT)
            .withClaim("user_name", USER)
            .withClaim("jti", JTI)
            .withClaim("grant_type", "password")
            .sign(algorithm)
    }

    private fun initTokenWithScopes(algorithm: Algorithm, exp: Instant, iat: Instant): DecodedJWT {
        val token = JWT.create()
            .withIssuer(ISS)
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .withAudience(CLIENT)
            .withSubject("i'm a trusted oauth client")
            .withArrayClaim(
                "scope", arrayOf(
                    "PROJECT.READ", "SUBJECT.CREATE",
                    "SUBJECT.READ", "MEASUREMENT.CREATE"
                )
            )
            .withClaim("client_id", "i'm a trusted oauth client")
            .withClaim("jti", JTI)
            .withClaim("grant_type", "client_credentials")
            .sign(algorithm)
        return JWT.decode(token)
    }
}
