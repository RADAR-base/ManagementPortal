package org.radarcns.management.security;

import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

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
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS));
        }
        return false;
    }

    /**
     * If the current user has a specific authority (security role).
     *
     * <p>The name of this method comes from the isUserInRole() method in the Servlet API</p>
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean isCurrentUserInRole(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
        }
        return false;
    }


    /**
     * Check if the given user has a project admin role in a given subject's project
     * @param user The user
     * @param subject The subject
     * @return True if the user has a project admin role for the given subject, false otherwise.
     *         Also returns false if user, subject, or both are <code>null</code>.
     */
    public static boolean isUserProjectAdminForSubject(User user, Subject subject) {
        if (user == null || subject == null) {
            return false;
        }
        Optional<String> subjectProject = subject.getUser().getRoles().stream()
            .filter(r -> r.getAuthority().getName().equals(AuthoritiesConstants.PARTICIPANT))
            .findFirst()
            .map(r -> r.getProject().getProjectName());
        if (!subjectProject.isPresent()) {
            // there is no participant role for this subject
            return false;
        }
        return user.getRoles().stream()
            .anyMatch(r -> r.getProject().getProjectName().equals(subjectProject.get())
                && r.getAuthority().getName().equals(AuthoritiesConstants.PROJECT_ADMIN));
    }

    /**
     * Default permissions check if a user can modify a subject. The user can modify the subject if
     * the user is the subject, or if the user is a SYS_ADMIN, or if the user is PROJECT_ADMIN for
     * the subject's project
     * @param user The user
     * @param subject The subject
     * @return true if the conditions are met, false otherwise. Also return false if user, subject
     *  or both are <code>null</code>.
     */
    public static boolean canUserModifySubject(User user, Subject subject) {
        if (user == null || subject == null) {
            return false;
        }
        return subject.getUser().getId() == user.getId() ||
            SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.SYS_ADMIN) ||
            SecurityUtils.isUserProjectAdminForSubject(user, subject);
    }
}
