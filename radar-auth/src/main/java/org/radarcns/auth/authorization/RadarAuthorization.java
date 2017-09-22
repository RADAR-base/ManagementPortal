package org.radarcns.auth.authorization;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authorization helper class for RADAR. This class is used to communicate with Management Portal to
 * check if the authenticated user is allowed to access the protected resources of a given subject
 * based on the authorities and project affiliations.
 */
public class RadarAuthorization {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorization.class);
    public static final String AUTHORITIES_CLAIM = "authorities";
    public static final String ROLES_CLAIM = "roles";

    public static boolean hasPermission(DecodedJWT token, Permission permission) {
        log.debug("Checking permission {} for user {}", permission.toString(),
            token.getSubject());
        Set<String> authsGranted = getAuthorities(token);
        // effectively takes intersection of both sets
        authsGranted.retainAll(Permissions.allowedAuthorities(permission));
        if (authsGranted.isEmpty()) {
            log.info("User {} does not have permission {}", token.getSubject(),
                permission.toString());
            return false;
        }
        return true;
    }

    public static boolean hasPermissionInProject(DecodedJWT token, Permission permission,
            String projectName) {
        log.debug("Checking permission {} for user {} in project {}", permission.toString(),
            token.getSubject(), projectName);
        Set<String> authsGranted = getAuthoritiesForProject(token, projectName);
        authsGranted.retainAll(Permissions.allowedAuthorities(permission));
        if (authsGranted.isEmpty()) {
            log.info("User {} does not have permission {} in project",
                token.getSubject(), permission.toString(), projectName);
            return false;
        }
        return true;
    }

    public static boolean isSuperUser(DecodedJWT token) {
        return token.getClaim(AUTHORITIES_CLAIM).asList(String.class)
            .contains(AuthoritiesConstants.SYS_ADMIN);
    }

    private static Set<String> getAuthoritiesForProject(DecodedJWT token, String projectName) {
        // get all project-based authorities
        return token.getClaim(ROLES_CLAIM).asList(String.class).stream()
            .filter(s -> s.startsWith(projectName + ":"))
            .map(s -> s.split(":")[1])
            .collect(Collectors.toSet());
    }

    private static Set<String> getAuthorities(DecodedJWT token) {
        // get all project-based authorities
        Set<String> result = token.getClaim(ROLES_CLAIM).asList(String.class).stream()
            .filter(s -> s.contains(":"))
            .map(s -> s.split(":")[1])
            .collect(Collectors.toSet());
        // also add non-project based authorities
        result.addAll(token.getClaim(AUTHORITIES_CLAIM).asList(String.class));
        return result;
    }
}
