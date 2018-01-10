package org.radarcns.auth.token;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RadarToken} based on JWT tokens.
 */
public class JwtRadarToken extends AbstractRadarToken {

    public static final String AUTHORITIES_CLAIM = "authorities";
    public static final String ROLES_CLAIM = "roles";
    public static final String SCOPE_CLAIM = "scope";
    public static final String SOURCES_CLAIM = "sources";
    public static final String GRANT_TYPE_CLAIM = "grant_type";

    private Map<String, List<String>> roles;
    private List<String> authorities;
    private List<String> scopes;
    private List<String> sources;
    private String grantType;
    private String subject;
    private Date issuedAt;
    private Date expiresAt;
    private List<String> audience;
    private String token;
    private String issuer;
    private String type;

    /**
     * Initialize this {@code JwtRadarToken} based on the {@link DecodedJWT}. All relevant
     * information will be parsed at construction time and no reference to the {@link DecodedJWT}
     * is kept. Therefore, modifying the passed in {@link DecodedJWT} after this has been
     * constructed will <strong>not</strong> update this object.
     * @param jwt the JWT token to use to initialize this object
     */
    public JwtRadarToken(DecodedJWT jwt) {
        roles = parseRoles(jwt);
        authorities = parseAuthorities(jwt);
        scopes = parseScopes(jwt);
        sources = parseSources(jwt);
        grantType = parseGrantType(jwt);
        subject = jwt.getSubject() == null ? "" : jwt.getSubject();
        issuedAt = jwt.getIssuedAt();
        expiresAt = jwt.getExpiresAt();
        audience = jwt.getAudience() == null ? Collections.emptyList() : jwt.getAudience();
        token = jwt.getToken() == null ? "" : jwt.getToken();
        issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer();
        type = jwt.getType() == null ? "" : jwt.getType();
    }

    @Override
    public Map<String, List<String>> getRoles() {
        return roles;
    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public List<String> getSources() {
        return sources;
    }

    @Override
    public String getGrantType() {
        return grantType;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Date getIssuedAt() {
        return issuedAt;
    }

    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public List<String> getAudience() {
        return audience;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getType() {
        return type;
    }

    private Map<String, List<String>> parseRoles(DecodedJWT jwt) {
        if (!jwt.getClaims().containsKey(ROLES_CLAIM)) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> result = new HashMap<>();
        List<String[]> parsedRoles = jwt.getClaim(ROLES_CLAIM).asList(String.class).stream()
                .filter(s -> s.contains(":"))
                .map(s -> s.split(":"))
                .collect(Collectors.toList());
        for (String[] role : parsedRoles) {
            if (!result.containsKey(role[0])) {
                result.put(role[0], new LinkedList<>());
            }
            result.get(role[0]).add(role[1]);
        }
        return result;
    }

    private List<String> parseAuthorities(DecodedJWT jwt) {
        return jwt.getClaims().containsKey(AUTHORITIES_CLAIM)
                ? jwt.getClaim(AUTHORITIES_CLAIM).asList(String.class)
                : Collections.emptyList();
    }

    private List<String> parseScopes(DecodedJWT jwt) {
        return jwt.getClaims().containsKey(SCOPE_CLAIM)
                ? jwt.getClaim(SCOPE_CLAIM).asList(String.class)
                : Collections.emptyList();
    }

    private List<String> parseSources(DecodedJWT jwt) {
        return jwt.getClaims().containsKey(SOURCES_CLAIM)
                ? jwt.getClaim(SOURCES_CLAIM).asList(String.class)
                : Collections.emptyList();
    }

    private String parseGrantType(DecodedJWT jwt) {
        return jwt.getClaims().containsKey(GRANT_TYPE_CLAIM)
                ? jwt.getClaim(GRANT_TYPE_CLAIM).asString()
                : "";
    }
}
