package org.radarbase.management.security;

import org.radarbase.auth.authentication.TokenValidator;
import org.radarbase.auth.exception.TokenValidationException;
import org.radarbase.auth.token.AuthorityReference;
import org.radarbase.auth.token.JwtRadarToken;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
    private final TokenValidator validator;
    private final AuthenticationManager authenticationManager;
    public static final String TOKEN_ATTRIBUTE = "jwt";
    private final List<AntPathRequestMatcher> ignoreUrls;
    private final UserRepository userRepository;

    /**
     * Authentication filter using given validator.
     * @param validator validates the JWT token.
     * @param authenticationManager authentication manager to pass valid authentication to.
     */
    public JwtAuthenticationFilter(TokenValidator validator,
            AuthenticationManager authenticationManager,
            UserRepository userRepository) {
        this.validator = validator;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.ignoreUrls = new ArrayList<>();
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
        SessionRadarToken token = null;
        if (session != null) {
            token = SessionRadarToken.from((RadarToken) session.getAttribute(TOKEN_ATTRIBUTE));
        }
        if (token == null) {
            try {
                token = SessionRadarToken.from(validator.validateAccessToken(getToken(httpRequest,
                        httpResponse)));
            } catch (TokenValidationException ex) {
                logger.error(ex.getMessage());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth2AccessToken.BEARER_TYPE);
                httpResponse.getOutputStream().println(
                        "{\"error\": \"" + "Unauthorized" + ",\n"
                                + "\"status\": \"" + HttpServletResponse.SC_UNAUTHORIZED + ",\n"
                                + "\"message\": \"" + ex.getMessage() + "\",\n"
                                + "\"path\": \"" + httpRequest.getRequestURI() + "\n"
                                + "\"}");
                return;
            }
        } else if (!token.isClientCredentials()) {
            var user = userRepository.findOneByLogin(token.getUsername());
            if (user.isPresent()) {
                var roles = user.get().getRoles().stream()
                        .map(role -> {
                            var auth = role.getRole();
                            return switch (role.getRole().scope()) {
                                case GLOBAL -> new AuthorityReference(auth);
                                case ORGANIZATION -> new AuthorityReference(auth,
                                        role.getOrganization().getName());
                                case PROJECT -> new AuthorityReference(auth,
                                        role.getProject().getProjectName());
                            };
                        })
                        .collect(Collectors.toSet());
                token = token.withRoles(roles);
            } else {
                session.removeAttribute(TOKEN_ATTRIBUTE);
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth2AccessToken.BEARER_TYPE);
                httpResponse.getOutputStream().println(
                        "{\"error\": \"" + "Unauthorized" + ",\n"
                                + "\"status\": \"" + HttpServletResponse.SC_UNAUTHORIZED + ",\n"
                                + "\"message\": \"User not found\",\n"
                                + "\"path\": \"" + httpRequest.getRequestURI() + "\n"
                                + "\"}");
                return;
            }
        }

        httpRequest.setAttribute(TOKEN_ATTRIBUTE, token);
        RadarAuthentication authentication = new RadarAuthentication(token);
        authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(httpRequest, httpResponse);
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

    private String getToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader
                .startsWith(OAuth2AccessToken.BEARER_TYPE)) {
            logger.error("No authorization header provided in the request");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth2AccessToken.BEARER_TYPE);
            throw new TokenValidationException("No " + OAuth2AccessToken.BEARER_TYPE + " token "
                    + "present in the request.");
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
    }
}
