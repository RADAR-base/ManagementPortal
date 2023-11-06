package org.radarbase.management.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.AccessTokenConverter
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier
import org.springframework.util.Assert
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import java.util.stream.Stream

/**
 * Implementation of [JwtAccessTokenConverter] for the RADAR-base ManagementPortal platform.
 *
 *
 * This class can accept an EC keypair as well as an RSA keypair for signing. EC signatures
 * are significantly smaller than RSA signatures.
 */
open class ManagementPortalJwtAccessTokenConverter(
    algorithm: Algorithm,
    verifiers: MutableList<JWTVerifier>,
    private val refreshTokenVerifiers: List<JWTVerifier>
) : JwtAccessTokenConverter {
    private val jsonParser = ObjectMapper().readerFor(
        MutableMap::class.java
    )
    private val tokenConverter: AccessTokenConverter

    /**
     * Returns JwtClaimsSetVerifier.
     *
     * @return the [JwtClaimsSetVerifier] used to verify the claim(s) in the JWT Claims Set
     */
    var jwtClaimsSetVerifier: JwtClaimsSetVerifier? = null
        /**
         * Sets JwtClaimsSetVerifier instance.
         *
         * @param jwtClaimsSetVerifier the [JwtClaimsSetVerifier] used to verify the claim(s)
         * in the JWT Claims Set
         */
        set(jwtClaimsSetVerifier) {
            Assert.notNull(jwtClaimsSetVerifier, "jwtClaimsSetVerifier cannot be null")
            field = jwtClaimsSetVerifier
        }
    private var algorithm: Algorithm? = null
    private val verifiers: MutableList<JWTVerifier>

    /**
     * Default constructor.
     * Creates [ManagementPortalJwtAccessTokenConverter] with
     * [DefaultAccessTokenConverter] as the accessTokenConverter with explicitly including
     * grant_type claim.
     */
    init {
        val accessToken = DefaultAccessTokenConverter()
        accessToken.setIncludeGrantType(true)
        tokenConverter = accessToken
        this.verifiers = verifiers
        setAlgorithm(algorithm)
    }

    override fun convertAccessToken(
        token: OAuth2AccessToken,
        authentication: OAuth2Authentication
    ): Map<String, *> {
        return tokenConverter.convertAccessToken(token, authentication)
    }

    override fun extractAccessToken(value: String, map: Map<String?, *>?): OAuth2AccessToken {
        var mapCopy = map?.toMutableMap()

        if (mapCopy?.containsKey(AccessTokenConverter.EXP) == true) {
            mapCopy[AccessTokenConverter.EXP] = (mapCopy[AccessTokenConverter.EXP] as Int).toLong()
        }
        return tokenConverter.extractAccessToken(value, mapCopy)
    }

    override fun extractAuthentication(map: Map<String?, *>?): OAuth2Authentication {
        return tokenConverter.extractAuthentication(map)
    }

    override fun setAlgorithm(algorithm: Algorithm) {
        this.algorithm = algorithm
        if (verifiers.isEmpty()) {
            verifiers.add(JWT.require(algorithm).withAudience(RES_MANAGEMENT_PORTAL).build())
        }
    }

    /**
     * Simplified the existing enhancing logic of
     * [JwtAccessTokenConverter.enhance].
     * Keeping the same logic.
     *
     *
     *
     * It mainly adds token-id for access token and access-token-id and token-id for refresh
     * token to the additional information.
     *
     *
     * @param accessToken    accessToken to enhance.
     * @param authentication current authentication of the token.
     * @return enhancedToken.
     */
    override fun enhance(
        accessToken: OAuth2AccessToken,
        authentication: OAuth2Authentication
    ): OAuth2AccessToken {
        // create new instance of token to enhance
        val resultAccessToken = DefaultOAuth2AccessToken(accessToken)
        // set additional information for access token
        val additionalInfoAccessToken: MutableMap<String, Any?> = HashMap(accessToken.additionalInformation)

        // add token id if not available
        var accessTokenId = accessToken.value
        if (!additionalInfoAccessToken.containsKey(JwtAccessTokenConverter.TOKEN_ID)) {
            additionalInfoAccessToken[JwtAccessTokenConverter.TOKEN_ID] = accessTokenId
        } else {
            accessTokenId = additionalInfoAccessToken[JwtAccessTokenConverter.TOKEN_ID] as String?
        }
        resultAccessToken.additionalInformation = additionalInfoAccessToken
        resultAccessToken.value = encode(accessToken, authentication)

        // add additional information for refresh-token
        val refreshToken = accessToken.refreshToken
        if (refreshToken != null) {
            val refreshTokenToEnhance = DefaultOAuth2AccessToken(accessToken)
            refreshTokenToEnhance.value = refreshToken.value
            // Refresh tokens do not expire unless explicitly of the right type
            refreshTokenToEnhance.expiration = null
            refreshTokenToEnhance.scope = accessToken.scope
            // set info of access token to refresh-token and add token-id and access-token-id for
            // reference.
            val refreshTokenInfo: MutableMap<String, Any?> = HashMap(accessToken.additionalInformation)
            refreshTokenInfo[JwtAccessTokenConverter.TOKEN_ID] = refreshTokenToEnhance.value
            refreshTokenInfo[JwtAccessTokenConverter.ACCESS_TOKEN_ID] = accessTokenId
            refreshTokenToEnhance.additionalInformation = refreshTokenInfo
            val encodedRefreshToken: DefaultOAuth2RefreshToken
            if (refreshToken is ExpiringOAuth2RefreshToken) {
                val expiration = refreshToken.expiration
                refreshTokenToEnhance.expiration = expiration
                encodedRefreshToken = DefaultExpiringOAuth2RefreshToken(
                    encode(refreshTokenToEnhance, authentication), expiration
                )
            } else {
                encodedRefreshToken = DefaultOAuth2RefreshToken(
                    encode(refreshTokenToEnhance, authentication)
                )
            }
            resultAccessToken.refreshToken = encodedRefreshToken
        }
        return resultAccessToken
    }

    override fun isRefreshToken(token: OAuth2AccessToken): Boolean {
        return token.additionalInformation?.containsKey(JwtAccessTokenConverter.ACCESS_TOKEN_ID) == true
    }

    override fun encode(accessToken: OAuth2AccessToken, authentication: OAuth2Authentication): String {
        // we need to override the encode method as well, Spring security does not know about
        // ECDSA, so it can not set the 'alg' header claim of the JWT to the correct value; here
        // we use the auth0 JWT implementation to create a signed, encoded JWT.
        val claims = convertAccessToken(accessToken, authentication)
        val builder = JWT.create()

        // add the string array claims
        Stream.of("aud", "sources", "roles", "authorities", "scope")
            .filter { key: String -> claims.containsKey(key) }
            .forEach { claim: String ->
                builder.withArrayClaim(
                    claim,
                    (claims[claim] as Collection<String>).toTypedArray<String>()
                )
            }

        // add the string claims
        Stream.of("sub", "iss", "user_name", "client_id", "grant_type", "jti", "ati")
            .filter { key: String -> claims.containsKey(key) }
            .forEach { claim: String -> builder.withClaim(claim, claims[claim] as String?) }

        // add the date claims, they are in seconds since epoch, we need milliseconds
        Stream.of("exp", "iat")
            .filter { key: String -> claims.containsKey(key) }
            .forEach { claim: String ->
                builder.withClaim(
                    claim,
                    Date.from(Instant.ofEpochSecond((claims[claim] as Long?)!!))
                )
            }
        return builder.sign(algorithm)
    }

    override fun decode(token: String): Map<String, Any> {
        val jwt = JWT.decode(token)
        val verifierToUse: List<JWTVerifier>
        val claims: MutableMap<String, Any>
        try {
            val decodedPayload = String(
                Base64.getUrlDecoder().decode(jwt.payload),
                StandardCharsets.UTF_8
            )
            claims = jsonParser.readValue(decodedPayload)
            if (claims.containsKey(AccessTokenConverter.EXP) && claims[AccessTokenConverter.EXP] is Int) {
                val intValue = claims[AccessTokenConverter.EXP] as Int?
                claims[AccessTokenConverter.EXP] = intValue!!
            }
            if (jwtClaimsSetVerifier != null) {
                jwtClaimsSetVerifier!!.verify(claims)
            }
            verifierToUse =
                if (claims[JwtAccessTokenConverter.ACCESS_TOKEN_ID] != null) refreshTokenVerifiers else verifiers
        } catch (ex: JsonProcessingException) {
            throw InvalidTokenException("Invalid token", ex)
        }
        for (verifier in verifierToUse) {
            try {
                verifier.verify(token)
                return claims
            } catch (sve: SignatureVerificationException) {
                logger.warn("Client presented a token with an incorrect signature")
            } catch (ex: JWTVerificationException) {
                logger.debug(
                    "Verifier {} with implementation {} did not accept token: {}",
                    verifier, verifier.javaClass, ex.message
                )
            }
        }
        throw InvalidTokenException("No registered validator could authenticate this token")
    }

    companion object {
        const val RES_MANAGEMENT_PORTAL = "res_ManagementPortal"
        private val logger = LoggerFactory.getLogger(ManagementPortalJwtAccessTokenConverter::class.java)
    }
}
