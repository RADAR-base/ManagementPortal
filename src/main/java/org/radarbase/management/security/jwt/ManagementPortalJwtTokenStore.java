package org.radarbase.management.security.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Adapted version of {@link org.springframework.security.oauth2.provider.token.store.JwtTokenStore}
 * which uses interface {@link JwtAccessTokenConverter} instead of tied instance.
 *
 * <p>
 * A {@link TokenStore} implementation that just reads data from the tokens themselves.
 * Not really a store since it never persists anything, and methods like
 * {@link #getAccessToken(OAuth2Authentication)} always return null. But
 * nevertheless a useful tool since it translates access tokens to and
 * from authentications. Use this wherever a{@link TokenStore} is needed,
 * but remember to use the same {@link JwtAccessTokenConverter}
 * instance (or one with the same verifier) as was used when the tokens were minted.
 * </p>
 *
 * @author Dave Syer
 * @author nivethika
 */
public class ManagementPortalJwtTokenStore implements TokenStore {
    private final JwtAccessTokenConverter jwtAccessTokenConverter;

    private ApprovalStore approvalStore;

    /**
     * Create a ManagementPortalJwtTokenStore with this token converter
     * (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtAccessTokenConverter JwtAccessTokenConverter used in the application.
     */
    public ManagementPortalJwtTokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
    }

    /**
     * Create a ManagementPortalJwtTokenStore with this token converter
     * (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtAccessTokenConverter JwtAccessTokenConverter used in the application.
     * @param approvalStore           TokenApprovalStore used in the application.
     */
    public ManagementPortalJwtTokenStore(JwtAccessTokenConverter jwtAccessTokenConverter,
            ApprovalStore approvalStore) {
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
        this.approvalStore = approvalStore;
    }

    /**
     * ApprovalStore to be used to validate and restrict refresh tokens.
     *
     * @param approvalStore the approvalStore to set
     */
    public void setApprovalStore(ApprovalStore approvalStore) {
        this.approvalStore = approvalStore;
    }


    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        return jwtAccessTokenConverter.extractAuthentication(jwtAccessTokenConverter.decode(token));
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        // this is not really a store where we persist
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        OAuth2AccessToken accessToken = convertAccessToken(tokenValue);

        if (jwtAccessTokenConverter.isRefreshToken(accessToken)) {
            throw new InvalidTokenException("Encoded token is a refresh token");
        }
        return accessToken;
    }

    private OAuth2AccessToken convertAccessToken(String tokenValue) {
        return jwtAccessTokenConverter
                .extractAccessToken(tokenValue, jwtAccessTokenConverter.decode(tokenValue));
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        // this is not really store where we persist
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken,
            OAuth2Authentication authentication) {
        // this is not really store where we persist
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        if (approvalStore != null) {
            OAuth2Authentication authentication = readAuthentication(tokenValue);
            if (authentication.getUserAuthentication() != null) {
                String userId = authentication.getUserAuthentication().getName();
                String clientId = authentication.getOAuth2Request().getClientId();
                Collection<Approval> approvals = approvalStore.getApprovals(userId, clientId);
                Collection<String> approvedScopes = new HashSet<>();
                for (Approval approval : approvals) {
                    if (approval.isApproved()) {
                        approvedScopes.add(approval.getScope());
                    }
                }
                if (!approvedScopes.containsAll(authentication.getOAuth2Request().getScope())) {
                    return null;
                }
            }
        }
        OAuth2AccessToken encodedRefreshToken = convertAccessToken(tokenValue);
        return createRefreshToken(encodedRefreshToken);
    }

    private OAuth2RefreshToken createRefreshToken(OAuth2AccessToken encodedRefreshToken) {
        if (!jwtAccessTokenConverter.isRefreshToken(encodedRefreshToken)) {
            throw new InvalidTokenException("Encoded token is not a refresh token");
        }
        if (encodedRefreshToken.getExpiration() != null) {
            return new DefaultExpiringOAuth2RefreshToken(encodedRefreshToken.getValue(),
                    encodedRefreshToken.getExpiration());
        }
        return new DefaultOAuth2RefreshToken(encodedRefreshToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        remove(token.getValue());
    }

    private void remove(String token) {
        if (approvalStore != null) {
            OAuth2Authentication auth = readAuthentication(token);
            String clientId = auth.getOAuth2Request().getClientId();
            Authentication user = auth.getUserAuthentication();
            if (user != null) {
                Collection<Approval> approvals = new ArrayList<>();
                for (String scope : auth.getOAuth2Request().getScope()) {
                    approvals.add(new Approval(user.getName(), clientId, scope, new Date(),
                            Approval.ApprovalStatus.APPROVED));
                }
                approvalStore.revokeApprovals(approvals);
            }
        }
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        // this is not really store where we persist
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        // We don't want to accidentally issue a token, and we have no way to reconstruct
        // the refresh token
        return null;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId,
            String userName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return Collections.emptySet();
    }
}
