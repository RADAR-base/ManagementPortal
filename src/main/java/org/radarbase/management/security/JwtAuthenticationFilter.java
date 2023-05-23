package org.radarbase.management.security;

import org.radarbase.auth.authentication.TokenValidator;
import org.radarbase.auth.authorization.AuthorityReference;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 29/09/2017.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String AUTHORIZATION_BEARER_HEADER = "Bearer";

    private final TokenValidator validator;
    private final AuthenticationManager authenticationManager;
    public static final String TOKEN_ATTRIBUTE = "jwt";
    private final List<AntPathRequestMatcher> ignoreUrls;
    private final UserRepository userRepository;
    private final boolean isOptional;

    /**
     * Authority references for given user. The user should have its roles mapped
     * from the database.
     * @param user user to get authority references from.
     * @return set of authority references.
     */
    public static Set<AuthorityReference> userAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> {
                    var auth = role.getRole();
                    return switch (role.getRole().getScope()) {
                        case GLOBAL -> new AuthorityReference(auth);
                        case ORGANIZATION -> new AuthorityReference(auth,
                                role.getOrganization().getName());
                        case PROJECT -> new AuthorityReference(auth,
                                role.getProject().getProjectName());
                    };
                })
                .collect(Collectors.toSet());
    }

    /**
     * Authentication filter using given validator. Authentication is mandatory.
     * @param validator validates the JWT token.
     * @param authenticationManager authentication manager to pass valid authentication to.
     * @param userRepository user repository to retrieve user details from.
     */
    public JwtAuthenticationFilter(TokenValidator validator,
            AuthenticationManager authenticationManager,
            UserRepository userRepository) {
        this(validator, authenticationManager, userRepository, false);
    }

    /**
     * Authentication filter using given validator.
     * @param validator validates the JWT token.
     * @param authenticationManager authentication manager to pass valid authentication to.
     * @param userRepository user repository to retrieve user details from.
     * @param isOptional do not fail if no authentication is provided
     */
    public JwtAuthenticationFilter(TokenValidator validator,
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            boolean isOptional) {
        this.validator = validator;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.ignoreUrls = new ArrayList<>();
        this.isOptional = isOptional;
    }

    /**
     * Do not use JWT authentication for given paths and HTTP method.
     * @param method HTTP method
     * @param antPatterns Ant wildcard pattern
     * @return the current filter
     */
    public JwtAuthenticationFilter skipUrlPattern(HttpMethod method, String... antPatterns) {
        for (String pattern : antPatterns) {
            ignoreUrls.add(new AntPathRequestMatcher(pattern, method.name()));
        }
        return this;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest httpRequest,
            @Nonnull HttpServletResponse httpResponse, @Nonnull FilterChain chain)
            throws IOException, ServletException {
        if (CorsUtils.isPreFlightRequest(httpRequest)) {
            logger.debug("Skipping JWT check for preflight request");
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        String stringToken = tokenFromHeader(httpRequest);
        RadarToken token = null;
        String exMessage = "No token provided";
        if (stringToken != null) {
            try {
                token = validator.validateBlocking(stringToken);
                logger.debug("Using token from header");
            } catch (TokenValidationException ex) {
                exMessage = ex.getMessage();
                logger.info("Failed to validate token from session: {}", exMessage);
            }
        }

        if (token == null) {
            token = tokenFromSession(session);
            if (token != null) {
                logger.debug("Using token from session");
            }
        }

        if (validateToken(token, httpRequest, httpResponse, session, exMessage)) {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private RadarToken tokenFromSession(HttpSession session) {
        if (session != null) {
            return (RadarToken) session.getAttribute(TOKEN_ATTRIBUTE);
        } else {
            return null;
        }
    }

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest httpRequest) {
        Optional<AntPathRequestMatcher> shouldNotFilterUrl = ignoreUrls.stream()
                .filter(pattern -> pattern.matches(httpRequest))
                .findAny();

        if (shouldNotFilterUrl.isPresent()) {
            logger.debug("Skipping JWT check for {} request", shouldNotFilterUrl.get());
            return true;
        }

        return false;
    }

    private String tokenFromHeader(HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader
                .startsWith(AUTHORIZATION_BEARER_HEADER)) {
            return authorizationHeader.substring(AUTHORIZATION_BEARER_HEADER.length()).trim();
        } else {
            return null;
        }
    }

    private boolean validateToken(RadarToken token, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, HttpSession session, String exMessage)
            throws IOException {
        if (token == null) {
            if (isOptional) {
                logger.debug("Skipping optional token");
                return true;
            } else {
                logger.error("Unauthorized - no valid token provided");
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                        AUTHORIZATION_BEARER_HEADER);
                httpResponse.getOutputStream().print(
                        "{\"error\": \"" + "Unauthorized" + ",\n"
                                + "\"status\": \"" + HttpServletResponse.SC_UNAUTHORIZED
                                + "\",\n"
                                + "\"message\": \"" + exMessage + "\",\n"
                                + "\"path\": \"" + httpRequest.getRequestURI() + "\n"
                                + "\"}");
                return false;
            }
        }

        RadarToken updatedToken = checkUser(token, httpRequest, httpResponse, session);
        if (updatedToken == null) {
            return false;
        }

        httpRequest.setAttribute(TOKEN_ATTRIBUTE, updatedToken);
        RadarAuthentication authentication = new RadarAuthentication(updatedToken);
        authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return true;
    }

    private RadarToken checkUser(RadarToken token, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, HttpSession session) throws IOException {
        String userName = token.getUsername();
        if (userName == null) {
            return token;
        }
        var user = userRepository.findOneByLogin(userName);
        if (user.isPresent()) {
            return token.copyWithRoles(userAuthorities(user.get()));
        } else {
            if (session != null) {
                session.removeAttribute(TOKEN_ATTRIBUTE);
            }
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTHORIZATION_BEARER_HEADER);
            httpResponse.getOutputStream().print(
                    "{\"error\": \"" + "Unauthorized" + ",\n"
                            + "\"status\": \"" + HttpServletResponse.SC_UNAUTHORIZED + ",\n"
                            + "\"message\": \"User not found\",\n"
                            + "\"path\": \"" + httpRequest.getRequestURI() + "\n"
                            + "\"}");
            return null;
        }
    }
}
