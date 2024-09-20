package org.radarbase.management.security.jwt

import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.approval.Approval
import org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus
import org.springframework.security.oauth2.provider.approval.ApprovalStore
import org.springframework.security.oauth2.provider.token.TokenStore
import java.util.*

/**
 * Adapted version of [org.springframework.security.oauth2.provider.token.store.JwtTokenStore]
 * which uses interface [JwtAccessTokenConverter] instead of tied instance.
 *
 *
 *
 * A [TokenStore] implementation that just reads data from the tokens themselves.
 * Not really a store since it never persists anything, and methods like
 * [.getAccessToken] always return null. But
 * nevertheless a useful tool since it translates access tokens to and
 * from authentications. Use this wherever a[TokenStore] is needed,
 * but remember to use the same [JwtAccessTokenConverter]
 * instance (or one with the same verifier) as was used when the tokens were minted.
 *
 *
 * @author Dave Syer
 * @author nivethika
 */
class ManagementPortalJwtTokenStore : TokenStore {
    private val jwtAccessTokenConverter: JwtAccessTokenConverter
    private var approvalStore: ApprovalStore? = null

    /**
     * Create a ManagementPortalJwtTokenStore with this token converter
     * (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtAccessTokenConverter JwtAccessTokenConverter used in the application.
     */
    constructor(jwtAccessTokenConverter: JwtAccessTokenConverter) {
        this.jwtAccessTokenConverter = jwtAccessTokenConverter
    }

    /**
     * Create a ManagementPortalJwtTokenStore with this token converter
     * (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtAccessTokenConverter JwtAccessTokenConverter used in the application.
     * @param approvalStore           TokenApprovalStore used in the application.
     */
    constructor(
        jwtAccessTokenConverter: JwtAccessTokenConverter,
        approvalStore: ApprovalStore?,
    ) {
        this.jwtAccessTokenConverter = jwtAccessTokenConverter
        this.approvalStore = approvalStore
    }

    /**
     * ApprovalStore to be used to validate and restrict refresh tokens.
     *
     * @param approvalStore the approvalStore to set
     */
    fun setApprovalStore(approvalStore: ApprovalStore?) {
        this.approvalStore = approvalStore
    }

    override fun readAuthentication(token: OAuth2AccessToken): OAuth2Authentication = readAuthentication(token.value)

    override fun readAuthentication(token: String): OAuth2Authentication =
        jwtAccessTokenConverter.extractAuthentication(jwtAccessTokenConverter.decode(token))

    override fun storeAccessToken(
        token: OAuth2AccessToken,
        authentication: OAuth2Authentication,
    ) {
        // this is not really a store where we persist
    }

    override fun readAccessToken(tokenValue: String): OAuth2AccessToken {
        val accessToken = convertAccessToken(tokenValue)
        if (jwtAccessTokenConverter.isRefreshToken(accessToken)) {
            throw InvalidTokenException("Encoded token is a refresh token")
        }
        return accessToken
    }

    private fun convertAccessToken(tokenValue: String): OAuth2AccessToken =
        jwtAccessTokenConverter
            .extractAccessToken(tokenValue, jwtAccessTokenConverter.decode(tokenValue))

    override fun removeAccessToken(token: OAuth2AccessToken) {
        // this is not really store where we persist
    }

    override fun storeRefreshToken(
        refreshToken: OAuth2RefreshToken,
        authentication: OAuth2Authentication,
    ) {
        // this is not really store where we persist
    }

    override fun readRefreshToken(tokenValue: String): OAuth2RefreshToken? {
        if (approvalStore != null) {
            val authentication = readAuthentication(tokenValue)
            if (authentication.userAuthentication != null) {
                val userId = authentication.userAuthentication.name
                val clientId = authentication.oAuth2Request.clientId
                val approvals = approvalStore!!.getApprovals(userId, clientId)
                val approvedScopes: MutableCollection<String> = HashSet()
                for (approval in approvals) {
                    if (approval.isApproved) {
                        approvedScopes.add(approval.scope)
                    }
                }
                if (!approvedScopes.containsAll(authentication.oAuth2Request.scope)) {
                    return null
                }
            }
        }
        val encodedRefreshToken = convertAccessToken(tokenValue)
        return createRefreshToken(encodedRefreshToken)
    }

    private fun createRefreshToken(encodedRefreshToken: OAuth2AccessToken): OAuth2RefreshToken {
        if (!jwtAccessTokenConverter.isRefreshToken(encodedRefreshToken)) {
            throw InvalidTokenException("Encoded token is not a refresh token")
        }
        return if (encodedRefreshToken.expiration != null) {
            DefaultExpiringOAuth2RefreshToken(
                encodedRefreshToken.value,
                encodedRefreshToken.expiration,
            )
        } else {
            DefaultOAuth2RefreshToken(encodedRefreshToken.value)
        }
    }

    override fun readAuthenticationForRefreshToken(token: OAuth2RefreshToken): OAuth2Authentication =
        readAuthentication(token.value)

    override fun removeRefreshToken(token: OAuth2RefreshToken) {
        remove(token.value)
    }

    private fun remove(token: String) {
        if (approvalStore != null) {
            val auth = readAuthentication(token)
            val clientId = auth.oAuth2Request.clientId
            val user = auth.userAuthentication
            if (user != null) {
                val approvals: MutableCollection<Approval> = ArrayList()
                for (scope in auth.oAuth2Request.scope) {
                    approvals.add(
                        Approval(
                            user.name,
                            clientId,
                            scope,
                            Date(),
                            ApprovalStatus.APPROVED,
                        ),
                    )
                }
                approvalStore!!.revokeApprovals(approvals)
            }
        }
    }

    override fun removeAccessTokenUsingRefreshToken(refreshToken: OAuth2RefreshToken) {
        // this is not really store where we persist
    }

    override fun getAccessToken(authentication: OAuth2Authentication): OAuth2AccessToken? {
        // We don't want to accidentally issue a token, and we have no way to reconstruct
        // the refresh token
        return null
    }

    override fun findTokensByClientIdAndUserName(
        clientId: String,
        userName: String,
    ): Collection<OAuth2AccessToken> = emptySet()

    override fun findTokensByClientId(clientId: String): Collection<OAuth2AccessToken> = emptySet()
}
