package org.radarcns.management.security;

import org.radarcns.auth.token.RadarToken;
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

    /**
     * Get the user name contianed in an Authentication object.
     *
     * @param authentication context authentication
     * @return user name or {@code null} if unknown.
     */
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

    /**
     * Parse the {@code "jwt"} attribute from given request.
     *
     * @param request servlet request
     * @return decoded JWT
     * @throws AccessDeniedException if the {@code "jwt"} attribute is missing or does not contain a
     *     decoded JWT
     */
    public static RadarToken getJWT(ServletRequest request) {
        Object token = request.getAttribute(JwtAuthenticationFilter.TOKEN_ATTRIBUTE);
        if (token == null) {
            // should not happen, the JwtAuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new AccessDeniedException("No token was found in the request context.");
        }
        if (!(token instanceof RadarToken)) {
            // should not happen, the JwtAuthenticationFilter will only set a DecodedJWT object
            throw new AccessDeniedException("Expected token to be of type org.radarcns"
                    + ".auth.token.RadarToken but was " + token.getClass().getName());
        }
        return (RadarToken) token;
    }


}
