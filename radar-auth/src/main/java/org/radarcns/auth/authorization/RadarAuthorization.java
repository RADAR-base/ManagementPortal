package org.radarcns.auth.authorization;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.auth.exception.NotAuthorizedException;
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

    /**
     * Check if the user authenticated with the given token has the given permission. Not taking
     * into account
     * @param token
     * @param permission
     */
    public static void checkPermission(DecodedJWT token, Permission permission) {
        log.debug("Checking permission {} for user {}", permission.toString(),
            token.getSubject());

        Set<String> authsGranted = getAuthorities(token);
        // effectively takes intersection of both sets
        authsGranted.retainAll(Permissions.allowedAuthorities(permission));
        if (authsGranted.isEmpty()) {
            log.info("User {} does not have permission {}", token.getSubject(),
                permission.toString());
            throw new NotAuthorizedException(String.format("User %s does not have permission %s",
                token.getSubject(), permission.toString()));
        }
    }

    public static void checkPermissionOnProject(DecodedJWT token, Permission permission,
            String projectName) {
        log.debug("Checking permission {} for user {} in project {}", permission.toString(),
            token.getSubject(), projectName);
        if (isSuperUser(token)) {
            return;
        }
        Set<String> authsGranted = getAuthoritiesForProject(token, projectName);
        authsGranted.retainAll(Permissions.allowedAuthorities(permission));
        if (authsGranted.isEmpty()) {
            log.info("User {} does not have permission {} in project {}",
                token.getSubject(), permission.toString(), projectName);
            throw new NotAuthorizedException(String.format("User %s does not have permission %s in "
                + "project %s", token.getSubject(), permission.toString(), projectName));
        }
    }

    public static void checkPermissionOnSubject(DecodedJWT token, Permission permission,
        String projectName, String subjectName) {
        log.debug("Checking permission {} for user {} on subject {} in project {}", permission.toString(),
            token.getSubject(), subjectName, projectName);
        if (isSuperUser(token)) {
            return;
        }
        // we're allowed to read our own data
        if (token.getSubject().equals(subjectName) && Permissions.allowedAuthorities(permission)
            .contains(AuthoritiesConstants.PARTICIPANT)) {
            return;
        }
        // if we're only a participant, and we're not the subject we request data for, we don't have access
        if (isJustParticipant(token, projectName)) {
            throw new NotAuthorizedException(String.format("User %s does not have permission %s in "
                    + "project %s for subject %s", token.getSubject(), permission.toString(),
                projectName, subjectName));
        }
        // otherwise we have other roles and we should check on a project level
        else {
            checkPermissionOnProject(token, permission, projectName);
        }
    }

    public static boolean isSuperUser(DecodedJWT token) {
        return token.getClaim(AUTHORITIES_CLAIM).asList(String.class)
            .contains(AuthoritiesConstants.SYS_ADMIN);
    }

    /**
     * Check if this user is just a participant in the project
     * @param token Token of the authenticated user
     * @param projectName Project to check
     * @return true if PARTICIPANT is the only authority of the user in the project, false otherwise
     */
    public static boolean isJustParticipant(DecodedJWT token, String projectName) {
        return token.getClaim(ROLES_CLAIM).asList(String.class).stream()
            .filter(r -> r.startsWith(projectName + ":"))
            .allMatch(r -> r.equals(projectName + ":" + AuthoritiesConstants.PARTICIPANT));

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
