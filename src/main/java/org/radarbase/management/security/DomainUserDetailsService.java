package org.radarbase.management.security;

import org.radarbase.auth.authorization.AuthorityReference;
import org.radarbase.auth.authorization.Permission;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.token.DataRadarToken;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.radarbase.auth.authorization.MPAuthorizationOracle.allowedRoles;
import static org.radarbase.management.security.JwtAuthenticationFilter.TOKEN_ATTRIBUTE;
import static org.radarbase.management.security.JwtAuthenticationFilter.userAuthorities;
import static org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter.RES_MANAGEMENT_PORTAL;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    private final HttpServletRequest httpRequest;

    public DomainUserDetailsService(
            UserRepository userRepository,
            @Nullable HttpServletRequest httpRequest) {
        this.userRepository = userRepository;
        this.httpRequest = httpRequest;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        User user = userRepository.findOneWithRolesByLogin(lowercaseLogin)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User " + lowercaseLogin + " was not found in the database"));
        if (!user.getActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin
                    + " was not activated");
        }
        addTokenToSession(user);

        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                lowercaseLogin,
                user.getPassword(),
                grantedAuthorities);
    }

    private void addTokenToSession(User user) {
        if (httpRequest == null) {
            return;
        }
        var roles = userAuthorities(user);

        var roleAuthorities = roles.stream()
                .map(AuthorityReference::getRole)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RoleAuthority.class)));

        var scopes = Arrays.stream(Permission.values())
                .filter(permission -> !Collections.disjoint(
                        allowedRoles(permission),
                        roleAuthorities))
                .map(Permission::scope)
                .collect(Collectors.toCollection(TreeSet::new));

        String subject = roleAuthorities.contains(RoleAuthority.PARTICIPANT)
                ? user.getLogin()
                : null;

        var token = new DataRadarToken(
                roles,
                scopes,
                List.of(),
                "password",
                subject,
                user.getLogin(),
                Instant.now(),
                Instant.MAX,
                List.of(RES_MANAGEMENT_PORTAL),
                null,
                null,
                "session",
                null);

        httpRequest.setAttribute(TOKEN_ATTRIBUTE, token);
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.setAttribute(TOKEN_ATTRIBUTE, token);
        }
    }
}
