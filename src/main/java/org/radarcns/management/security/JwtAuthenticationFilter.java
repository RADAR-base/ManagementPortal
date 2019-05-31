package org.radarcns.management.security;

import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.exception.TokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dverbeec on 29/09/2017.
 */
public class JwtAuthenticationFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final TokenValidator validator;
    public static final String TOKEN_ATTRIBUTE = "jwt";

    public JwtAuthenticationFilter(TokenValidator validator) {
        this.validator = validator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
            IOException, ServletException {
        if (CorsUtils.isPreFlightRequest((HttpServletRequest) request)) {
            log.debug("Skipping JWT check for preflight request");
            chain.doFilter(request, response);
            return;
        }

        if (((HttpServletRequest) request).getRequestURI().contains("management/health")) {
            log.debug("Skipping JWT check for Health check request");
            chain.doFilter(request, response);
            return;
        }

        try {
            request.setAttribute(TOKEN_ATTRIBUTE,
                    validator.validateAccessToken(getToken(request, response)));
            log.debug("Request authenticated successfully");
            chain.doFilter(request, response);
        } catch (TokenValidationException ex) {
            log.error(ex.getMessage());
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth2AccessToken.BEARER_TYPE);
            res.getOutputStream().println(
                    "{\"error\": \"" + "Unauthorized" + ",\n"
                            + "\"status\": \"" + HttpServletResponse.SC_UNAUTHORIZED + ",\n"
                            + "\"message\": \"" + ex.getMessage() + ",\n"
                            + "\"path\": \"" + ((HttpServletRequest) request).getRequestURI() + "\n"
                            + "\"}");
        }
    }

    private String getToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader
                .startsWith(OAuth2AccessToken.BEARER_TYPE)) {
            log.error("No authorization header provided in the request");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth2AccessToken.BEARER_TYPE);
            throw new TokenValidationException("No " + OAuth2AccessToken.BEARER_TYPE + " token "
                    + "present in the request.");
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
    }
}
