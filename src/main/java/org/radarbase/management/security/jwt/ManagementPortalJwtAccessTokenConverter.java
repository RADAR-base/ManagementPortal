package org.radarbase.management.security.jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import org.radarbase.auth.authentication.AlgorithmLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.util.Assert;

/**
 * Implementation of {@link JwtAccessTokenConverter} for the RADAR-base ManagementPortal platform.
 *
 * <p>This class can accept an EC keypair as well as an RSA keypair for signing. EC signatures
 * are significantly smaller than RSA signatures.</p>
 */
public class ManagementPortalJwtAccessTokenConverter implements JwtAccessTokenConverter {

    public static final String RES_MANAGEMENT_PORTAL = "res_ManagementPortal";

    private final JsonParser jsonParser = JsonParserFactory.create();

    private static final Logger logger =
            LoggerFactory.getLogger(ManagementPortalJwtAccessTokenConverter.class);

    private final AccessTokenConverter tokenConverter;

    private JwtClaimsSetVerifier jwtClaimsSetVerifier;

    private Algorithm algorithm;

    private final List<JWTVerifier> verifiers;

    private final List<JWTVerifier> refreshTokenVerifiers;


    /**
     * Default constructor.
     * Creates {@link ManagementPortalJwtAccessTokenConverter} with
     * {@link DefaultAccessTokenConverter} as the accessTokenConverter with explicitly including
     * grant_type claim.
     */
    public ManagementPortalJwtAccessTokenConverter(
            Algorithm algorithm,
            List<JWTVerifier> verifiers,
            List<JWTVerifier> refreshTokenVerifiers) {
        this.refreshTokenVerifiers = refreshTokenVerifiers;
        DefaultAccessTokenConverter accessToken = new DefaultAccessTokenConverter();
        accessToken.setIncludeGrantType(true);
        this.tokenConverter = accessToken;
        this.verifiers = verifiers;
        setAlgorithm(algorithm);
    }

    /**
     * Returns JwtClaimsSetVerifier.
     *
     * @return the {@link JwtClaimsSetVerifier} used to verify the claim(s) in the JWT Claims Set
     */
    public JwtClaimsSetVerifier getJwtClaimsSetVerifier() {
        return this.jwtClaimsSetVerifier;
    }

    /**
     * Sets JwtClaimsSetVerifier instance.
     *
     * @param jwtClaimsSetVerifier the {@link JwtClaimsSetVerifier} used to verify the claim(s)
     *                             in the JWT Claims Set
     */
    public void setJwtClaimsSetVerifier(JwtClaimsSetVerifier jwtClaimsSetVerifier) {
        Assert.notNull(jwtClaimsSetVerifier, "jwtClaimsSetVerifier cannot be null");
        this.jwtClaimsSetVerifier = jwtClaimsSetVerifier;
    }

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token,
            OAuth2Authentication authentication) {
        return tokenConverter.convertAccessToken(token, authentication);
    }

    @Override
    public OAuth2AccessToken extractAccessToken(String value, Map<String, ?> map) {
        return tokenConverter.extractAccessToken(value, map);
    }

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        return tokenConverter.extractAuthentication(map);
    }

    @Override
    public final void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        if (verifiers.isEmpty()) {
            this.verifiers.add(AlgorithmLoader.buildVerifier(algorithm, RES_MANAGEMENT_PORTAL));
        }
    }


    /**
     * Simplified the existing enhancing logic of
     * {@link JwtAccessTokenConverter#enhance(OAuth2AccessToken, OAuth2Authentication)}.
     * Keeping the same logic.
     *
     * <p>
     * It mainly adds token-id for access token and access-token-id and token-id for refresh
     * token to the additional information.
     * </p>
     *
     * @param accessToken    accessToken to enhance.
     * @param authentication current authentication of the token.
     * @return enhancedToken.
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        // create new instance of token to enhance
        DefaultOAuth2AccessToken resultAccessToken = new DefaultOAuth2AccessToken(accessToken);
        // set additional information for access token
        Map<String, Object> additionalInfoAccessToken =
                new HashMap<>(accessToken.getAdditionalInformation());

        // add token id if not available
        String accessTokenId = accessToken.getValue();

        if (!additionalInfoAccessToken.containsKey(TOKEN_ID)) {
            additionalInfoAccessToken.put(TOKEN_ID, accessTokenId);
        } else {
            accessTokenId = (String) additionalInfoAccessToken.get(TOKEN_ID);
        }

        resultAccessToken
                .setAdditionalInformation(additionalInfoAccessToken);

        resultAccessToken.setValue(encode(accessToken, authentication));

        // add additional information for refresh-token
        OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
        if (refreshToken != null) {

            DefaultOAuth2AccessToken refreshTokenToEnhance =
                    new DefaultOAuth2AccessToken(accessToken);
            refreshTokenToEnhance.setValue(refreshToken.getValue());
            // Refresh tokens do not expire unless explicitly of the right type
            refreshTokenToEnhance.setExpiration(null);
            refreshTokenToEnhance.setScope(accessToken.getScope());
            // set info of access token to refresh-token and add token-id and access-token-id for
            // reference.

            try {
                Map<String, Object> claims = jsonParser
                        .parseMap(JwtHelper.decode(refreshToken.getValue()).getClaims());
                if (claims.containsKey(TOKEN_ID)) {
                    refreshTokenToEnhance.setValue(claims.get(TOKEN_ID).toString());
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Could not decode refresh token ", e);
            }

            Map<String, Object> refreshTokenInfo =
                    new HashMap<>(accessToken.getAdditionalInformation());
            refreshTokenInfo.put(TOKEN_ID, refreshTokenToEnhance.getValue());
            refreshTokenInfo.put(ACCESS_TOKEN_ID, accessTokenId);

            refreshTokenToEnhance.setAdditionalInformation(refreshTokenInfo);

            DefaultOAuth2RefreshToken encodedRefreshToken;
            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                Date expiration = ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration();
                refreshTokenToEnhance.setExpiration(expiration);

                encodedRefreshToken = new DefaultExpiringOAuth2RefreshToken(
                        encode(refreshTokenToEnhance, authentication), expiration);
            } else {
                encodedRefreshToken = new DefaultOAuth2RefreshToken(
                        encode(refreshTokenToEnhance, authentication));
            }
            resultAccessToken.setRefreshToken(encodedRefreshToken);
        }
        return resultAccessToken;
    }

    @Override
    public boolean isRefreshToken(OAuth2AccessToken token) {
        return token.getAdditionalInformation().containsKey(ACCESS_TOKEN_ID);
    }

    @Override
    public String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        // we need to override the encode method as well, Spring security does not know about
        // ECDSA so it can not set the 'alg' header claim of the JWT to the correct value; here
        // we use the auth0 JWT implementation to create a signed, encoded JWT.
        Map<String, ?> claims = convertAccessToken(accessToken, authentication);

        JWTCreator.Builder builder = JWT.create();

        // add the string array claims
        Stream.of("aud", "sources", "roles", "authorities", "scope")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withArrayClaim(claim,
                        ((Collection<String>) claims.get(claim)).toArray(new String[0])));

        // add the string claims
        Stream.of("sub", "iss", "user_name", "client_id", "grant_type", "jti", "ati")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim, (String) claims.get(claim)));

        // add the date claims, they are in seconds since epoch, we need milliseconds
        Stream.of("exp", "iat")
                .filter(claims::containsKey)
                .forEach(claim -> builder.withClaim(claim,
                        Date.from(Instant.ofEpochSecond((Long)claims.get(claim)))));

        return builder.sign(algorithm);
    }

    @Override
    public Map<String, Object> decode(String token) {
        Jwt jwt = JwtHelper.decode(token);
        String claimsStr = jwt.getClaims();
        Map<String, Object> claims = jsonParser.parseMap(claimsStr);
        if (claims.containsKey(EXP) && claims.get(EXP) instanceof Integer) {
            Integer intValue = (Integer) claims.get(EXP);
            claims.put(EXP, Long.valueOf(intValue));
        }
        if (this.getJwtClaimsSetVerifier() != null) {
            this.getJwtClaimsSetVerifier().verify(claims);
        }

        List<JWTVerifier> verifierToUse = claims.get(ACCESS_TOKEN_ID) != null
                ? refreshTokenVerifiers : verifiers;

        for (JWTVerifier verifier : verifierToUse) {
            try {
                verifier.verify(token);
                return claims;
            } catch (SignatureVerificationException sve) {
                logger.warn("Client presented a token with an incorrect signature");
            } catch (JWTVerificationException ex) {
                logger.debug("Verifier {} with implementation {} did not accept token: {}",
                        verifier.toString(), verifier.getClass().toString(), ex.getMessage());
            }
        }

        throw new InvalidTokenException("No registered validator could authenticate this token");
    }

}
