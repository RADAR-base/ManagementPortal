package org.radarbase.auth.token;

import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarbase.auth.authorization.AuthoritiesConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementation of {@link RadarToken} based on JWT tokens.
 */
public class JwtRadarToken extends AbstractRadarToken {
    private static final Pattern ROLE_SEPARATOR_PATTERN = Pattern.compile(":");

    public static final String AUTHORITIES_CLAIM = "authorities";
    public static final String ROLES_CLAIM = "roles";
    public static final String SCOPE_CLAIM = "scope";
    public static final String SOURCES_CLAIM = "sources";
    public static final String GRANT_TYPE_CLAIM = "grant_type";
    public static final String CLIENT_ID_CLAIM = "client_id";
    public static final String USER_NAME_CLAIM = "user_name";

    private final Set<AuthorityReference> roles;
    private final List<String> authorities;
    private final List<String> scopes;
    private final List<String> sources;
    private final String grantType;
    private final String subject;
    private final Date issuedAt;
    private final Date expiresAt;
    private final List<String> audience;
    private final String token;
    private final String issuer;
    private final String type;
    private final String clientId;
    private final DecodedJWT jwt;
    private final String username;

    /**
     * Initialize this {@code JwtRadarToken} based on the {@link DecodedJWT}. All relevant
     * information will be parsed at construction time and no reference to the {@link DecodedJWT}
     * is kept. Therefore, modifying the passed in {@link DecodedJWT} after this has been
     * constructed will <strong>not</strong> update this object.
     * @param jwt the JWT token to use to initialize this object
     */
    public JwtRadarToken(DecodedJWT jwt) {
        this.jwt = jwt;
        authorities = emptyIfNull(jwt.getClaim(AUTHORITIES_CLAIM).asList(String.class));
        roles = Stream.concat(
                authorities.stream()
                        .map(AuthoritiesConstants::valueOfRoleOrNull)
                        .filter(r -> r != null && r.scope() == AuthoritiesConstants.Scope.GLOBAL)
                        .map(AuthorityReference::new),
                parseRoles(jwt))
                .collect(toUnmodifiableSet());

        Claim scopeClaim = jwt.getClaim(SCOPE_CLAIM);
        String scopeClaimString = scopeClaim.asString();

        if (scopeClaimString != null) {
            scopes = Arrays.asList(scopeClaimString.split(" "));
        } else {
            List<String> scopeClaimList = scopeClaim.asList(String.class);
            scopes = requireNonNullElseGet(scopeClaimList, Collections::emptyList);
        }

        sources = emptyIfNull(jwt.getClaim(SOURCES_CLAIM).asList(String.class));
        grantType = emptyIfNull(jwt.getClaim(GRANT_TYPE_CLAIM).asString());
        subject = emptyIfNull(jwt.getSubject());
        username = emptyIfNull(jwt.getClaim(USER_NAME_CLAIM).asString());
        issuedAt = jwt.getIssuedAt();
        expiresAt = jwt.getExpiresAt();
        audience = emptyIfNull(jwt.getAudience());
        token = emptyIfNull(jwt.getToken());
        issuer = emptyIfNull(jwt.getIssuer());
        type = emptyIfNull(jwt.getType());
        clientId = jwt.getClaim(CLIENT_ID_CLAIM).asString();
    }

    @Override
    public Set<AuthorityReference> getRoles() {
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
    public String getUsername() {
        return username;
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

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClaimString(String name) {
        return jwt.getClaim(name).asString();
    }

    @Override
    public List<String> getClaimList(String name) {
        try {
            return jwt.getClaim(name).asList(String.class);
        } catch (JWTDecodeException ex) {
            return null;
        }
    }

    private Stream<AuthorityReference> parseRoles(DecodedJWT jwt) {
        return emptyIfNull(jwt.getClaim(ROLES_CLAIM).asList(String.class)).stream()
                .filter(s -> s != null && !s.isBlank())
                .map(ROLE_SEPARATOR_PATTERN::split)
                .map(v -> v.length == 1 || v[1].isEmpty()
                        ? new AuthorityReference(v[0])
                        : new AuthorityReference(v[1], v[0]));
    }

    private static String emptyIfNull(String string) {
        return string != null ? string : "";
    }

    private static List<String> emptyIfNull(List<String> list) {
        return requireNonNullElseGet(list, Collections::emptyList);
    }
}
