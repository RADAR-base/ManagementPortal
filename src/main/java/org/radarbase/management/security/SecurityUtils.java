package org.radarbase.management.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (principal instanceof String) {
            return (String) authentication.getPrincipal();
        } else if (principal instanceof Authentication) {
            return ((Authentication)principal).getName();
        } else {
            return null;
        }
    }
}
