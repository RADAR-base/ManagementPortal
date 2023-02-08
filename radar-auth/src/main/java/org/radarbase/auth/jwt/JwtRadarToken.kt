package org.radarbase.auth.jwt

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.AbstractRadarToken
import org.radarbase.auth.token.AuthorityReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * Implementation of [RadarToken] based on JWT tokens.
 *
 * Initialize this `JwtRadarToken` based on the [DecodedJWT]. All relevant
 * information will be parsed at construction time and no reference to the [DecodedJWT]
 * is kept. Therefore, modifying the passed in [DecodedJWT] after this has been
 * constructed will **not** update this object.
 * @param jwt the JWT token to use to initialize this object
 */
class JwtRadarToken(private val jwt: DecodedJWT) : AbstractRadarToken() {
    override val roles: Set<AuthorityReference> = (jwt.parseAuthorities() + jwt.parseRoles()).toSet()
    override val scopes: Set<String>
    override val sources: List<String> = jwt.listClaim(SOURCES_CLAIM)
    override val grantType: String = jwt.stringClaim(GRANT_TYPE_CLAIM)
    override val subject: String = jwt.subject ?: ""
    override val issuedAt: Date = jwt.issuedAt
    override val expiresAt: Date = jwt.expiresAt
    override val audience: List<String> = jwt.audience ?: emptyList()
    override val token: String = jwt.token ?: ""
    override val issuer: String = jwt.issuer ?: ""
    override val type: String = jwt.type ?: ""
    override val clientId: String = jwt.stringClaim(CLIENT_ID_CLAIM)
    override val username: String = jwt.stringClaim(USER_NAME_CLAIM)

    init {
        val scopeClaim = jwt.getClaim(SCOPE_CLAIM)
        val scopeClaimString = scopeClaim.asString()
        scopes = scopeClaimString?.parseScopes()
            ?: jwt.listClaim(SCOPE_CLAIM).toSet()
    }

    override fun getClaimString(name: String): String? {
        return jwt.getClaim(name).asString()
    }

    override fun getClaimList(name: String): List<String> {
        return try {
            jwt.listClaim(name)
        } catch (ex: JWTDecodeException) {
            emptyList()
        }
    }

    companion object {
        private const val AUTHORITIES_CLAIM = "authorities"
        const val ROLES_CLAIM = "roles"
        const val SCOPE_CLAIM = "scope"
        const val SOURCES_CLAIM = "sources"
        const val GRANT_TYPE_CLAIM = "grant_type"
        const val CLIENT_ID_CLAIM = "client_id"
        const val USER_NAME_CLAIM = "user_name"

        private fun DecodedJWT.listClaim(name: String): List<String> = getClaim(name)
            .asList(String::class.java)
            ?.filterTo(ArrayList()) { s: String? -> !s.isNullOrBlank() }
            ?: emptyList()

        private fun DecodedJWT.stringClaim(name: String) = getClaim(name)
            .asString()
            ?: ""

        private fun String.parseScopes() = split(' ')
            .filterTo(mutableSetOf()) { it.isNotBlank() }

        private fun DecodedJWT.parseAuthorities(): Sequence<AuthorityReference> = listClaim(
            AUTHORITIES_CLAIM
        )
            .asSequence()
            .mapNotNull { RoleAuthority.valueOfAuthorityOrNull(it) }
            .filter { it.scope == RoleAuthority.Scope.GLOBAL }
            .map { AuthorityReference(it) }

        private fun DecodedJWT.parseRoles(): Sequence<AuthorityReference> = listClaim(ROLES_CLAIM)
            .asSequence()
            .mapNotNull { input ->
                val v = input.split(':')
                try {
                    if (v.size == 1 || v[1].isEmpty()) {
                        AuthorityReference(v[0])
                    } else {
                        AuthorityReference(v[1], v[0])
                    }
                } catch (ex: IllegalArgumentException) {
                    null
                }
            }
    }
}
