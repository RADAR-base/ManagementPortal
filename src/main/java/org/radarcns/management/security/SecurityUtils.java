package org.radarcns.management.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.ServletRequest;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    public static String getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return getUserName(securityContext.getAuthentication());
    }

    public static String getUserName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    public static DecodedJWT getJWT(ServletRequest request) {
        Object jwt = request.getAttribute(JwtAuthenticationFilter.TOKEN_ATTRIBUTE);
        if (jwt == null) {
            // should not happen, the JwtAuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new AccessDeniedException("No token was found in the request context.");
        }
        if (!(jwt instanceof DecodedJWT)) {
            // should not happen, the JwtAuthenticationFilter will only set a DecodedJWT object
            throw new AccessDeniedException("Expected token to be of type DecodedJWT but was "
                    + jwt.getClass().getName());
        }
        return (DecodedJWT) jwt;
    }


}
