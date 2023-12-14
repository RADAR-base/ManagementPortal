package org.radarbase.auth.jwt

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory

class JwtTokenVerifier(
    private val algorithm: String,
    private val verifier: JWTVerifier,
) : TokenVerifier {
    override fun verify(token: String): RadarToken = try {
        val jwt = verifier.verify(token)

        // Do not print full token with signature to avoid exposing valid token in logs.
        logger.debug("Verified JWT header {} and payload {}", jwt.header, jwt.payload)

        jwt.toRadarToken()
    } catch (ex: Throwable) {
        when (ex) {
            is SignatureVerificationException -> logger.debug("Client presented a token with an incorrect signature.")
            is JWTVerificationException -> logger.debug("Verifier {} did not accept token: {}", verifier.javaClass, ex.message)
        }
        throw ex
    }

    override fun toString(): String = "JwtTokenVerifier(algorithm=$algorithm)"

    companion object {
        private val logger = LoggerFactory.getLogger(JwtTokenVerifier::class.java)

        const val AUTHORITIES_CLAIM = "authorities"
        const val ROLES_CLAIM = "roles"
        const val SCOPE_CLAIM = "scope"
        const val SOURCES_CLAIM = "sources"
        const val GRANT_TYPE_CLAIM = "grant_type"
        const val CLIENT_ID_CLAIM = "client_id"
        const val USER_NAME_CLAIM = "user_name"

        fun DecodedJWT.toRadarToken(): RadarToken {
            val claims = claims

            return DataRadarToken(
                roles = claims.parseRoles(),
                scopes = claims.stringListClaim(SCOPE_CLAIM)?.toSet() ?: emptySet(),
                sources = claims.stringListClaim(SOURCES_CLAIM) ?: emptyList(),
                grantType = claims.stringClaim(GRANT_TYPE_CLAIM),
                subject = subject,
                issuedAt = issuedAtAsInstant,
                expiresAt = expiresAtAsInstant,
                audience = audience ?: emptyList(),
                token = token,
                issuer = issuer,
                type = type,
                clientId = claims.stringClaim(CLIENT_ID_CLAIM),
                username = claims.stringClaim(USER_NAME_CLAIM),
            )
        }
        fun Map<String, Claim?>.stringListClaim(name: String): List<String>? {
            val claim = get(name) ?: return null
            val claimList = try {
                claim.asList(String::class.java)
            } catch (ex: JWTDecodeException) {
                // skip
                null
            }
            val claims = claimList
                ?: claim.asString()?.split(' ')
                ?: return null

            return claims.mapNotNull { it?.trimNotEmpty() }
        }

        fun Map<String, Claim?>.stringClaim(name: String): String? = get(name)?.asString()
            ?.trimNotEmpty()

        private fun String.trimNotEmpty(): String? = trim()
            .takeIf { it.isNotEmpty() }

        private fun Map<String, Claim?>.parseRoles(): Set<AuthorityReference> = buildSet {
            stringListClaim(AUTHORITIES_CLAIM)?.forEach {
                val role = RoleAuthority.valueOfAuthorityOrNull(it)
                if (role?.scope == RoleAuthority.Scope.GLOBAL) {
                    add(AuthorityReference(role))
                }
            }
            stringListClaim(ROLES_CLAIM)?.forEach { input ->
                val v = input.split(':')
                try {
                    add(
                        if (v.size == 1 || v[1].isEmpty()) {
                            AuthorityReference(v[0])
                        } else {
                            AuthorityReference(v[1], v[0])
                        }
                    )
                } catch (ex: IllegalArgumentException) {
                    // skip
                }
            }
        }
    }
}
