package org.radarcns.management.security.jwt;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
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
 *  Implementation of {@link JwtAccessTokenConverter} for the RADAR-base ManagementPortal platform.
 *
 * <p>This class can accept an EC keypair as well as an RSA keypair for signing. EC signatures
 * are significantly smaller than RSA signatures.</p>
 */
public class RadarJwtAccessTokenConverter implements JwtAccessTokenConverter {

    public static final String RES_MANAGEMENT_PORTAL = "res_ManagementPortal";

    private final JsonParser objectMapper = JsonParserFactory.create();

    private static final Logger logger = LoggerFactory
            .getLogger(RadarJwtAccessTokenConverter.class);

    private AccessTokenConverter tokenConverter;

    private JwtClaimsSetVerifier
            jwtClaimsSetVerifier = new NoOpJwtClaimsSetVerifier();

    private Algorithm algorithm;
    private JWTVerifier verifier;


    /**
     * Default constructor.
     * Creates {@link RadarJwtAccessTokenConverter} with {@link DefaultAccessTokenConverter} as
     * the accessTokenConverter with explicitly including grant_type claim.
     */
    public RadarJwtAccessTokenConverter() {
        DefaultAccessTokenConverter accessToken = new DefaultAccessTokenConverter();
        accessToken .setIncludeGrantType(true);
        this.tokenConverter = accessToken;
    }

    /**
     * @return the tokenConverter in use
     */
    public AccessTokenConverter getAccessTokenConverter() {
        return tokenConverter;
    }

    /**
     * @return the {@link JwtClaimsSetVerifier} used to verify the claim(s) in the JWT Claims Set
     */
    public JwtClaimsSetVerifier getJwtClaimsSetVerifier() {
        return this.jwtClaimsSetVerifier;
    }

    /**
     * @param jwtClaimsSetVerifier the {@link JwtClaimsSetVerifier} used to verify the claim(s) in the JWT Claims Set
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

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        verifier = JWT.require(algorithm).build();
    }


    /**
     * Simplified the existing enhancing logic of
     * {@link JwtAccessTokenConverter#enhance(OAuth2AccessToken, OAuth2Authentication)}.
     * Keeping the same logic.
     *
     * It mainly adds token-id for access token and access-token-id and token-id for refresh
     * token to the additional information.
     *
     * @param accessToken accessToken to enhance.
     * @param authentication current authentication of the token.
     * @return enhancedToken.
     */
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

        // set additional information for access token
        Map<String, Object> additionalInfoAccessToken =
                new HashMap<>(accessToken.getAdditionalInformation());

        // add token id if not available
        String accessTokenId = accessToken.getValue();

        if (!additionalInfoAccessToken.containsKey(TOKEN_ID)) {
            additionalInfoAccessToken.put(TOKEN_ID, accessTokenId);
        }

        ((DefaultOAuth2AccessToken) accessToken)
                .setAdditionalInformation(additionalInfoAccessToken);

        ((DefaultOAuth2AccessToken) accessToken).setValue(encode(accessToken, authentication));

        // add additional information for refresh-token
        OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
        if (refreshToken != null) {

            DefaultOAuth2AccessToken refreshTokenToEnhance = new DefaultOAuth2AccessToken
                    (refreshToken.getValue());
            // Refresh tokens do not expire unless explicitly of the right type
            refreshTokenToEnhance.setExpiration(null);

            // I don't really get what is the point of doing this.
            try {
                Map<String, Object> claims = objectMapper
                        .parseMap(JwtHelper.decode(refreshToken.getValue()).getClaims());
                if (claims.containsKey(TOKEN_ID)) {
                    refreshTokenToEnhance.setValue(claims.get(TOKEN_ID).toString());
                }
            } catch (IllegalArgumentException e) {
                logger.error("Could not decode refresh token ", e);
            }
            // set info of access token to refresh-token and add token-id and access-token-id for
            // reference.

            Map<String, Object> refreshTokenInfo = new HashMap<>(
                    accessToken.getAdditionalInformation());
            refreshTokenInfo.put(TOKEN_ID, refreshToken.getValue());
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
            ((DefaultOAuth2AccessToken)accessToken).setRefreshToken(encodedRefreshToken);
        }
        return accessToken;
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
                .forEach(claim -> builder.withClaim(claim, new Date(
                        ((Long) claims.get(claim)) * 1000)));

        return builder.sign(algorithm);
    }

    @Override
    public Map<String, Object> decode(String token) {
        try {
            verifier.verify(token);
            Jwt jwt = JwtHelper.decode(token);
            String claimsStr = jwt.getClaims();
            Map<String, Object> claims = objectMapper.parseMap(claimsStr);
            if (claims.containsKey(EXP) && claims.get(EXP) instanceof Integer) {
                Integer intValue = (Integer) claims.get(EXP);
                claims.put(EXP, new Long(intValue));
            }
            this.getJwtClaimsSetVerifier().verify(claims);
            return claims;
        } catch (Exception e) {
            logger.debug("Cannot convert access token ", e);
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }




    private class NoOpJwtClaimsSetVerifier implements JwtClaimsSetVerifier {
        @Override
        public void verify(Map<String, Object> claims) throws InvalidTokenException {
        }
    }
}
