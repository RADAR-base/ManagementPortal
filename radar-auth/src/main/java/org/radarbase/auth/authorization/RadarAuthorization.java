package org.radarbase.auth.authorization;

import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization helper class for RADAR. This class checks if the authenticated user is allowed to
 * access the protected resources of a given subject based on the authorities and project
 * affiliations.
 */
public final class RadarAuthorization {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorization.class);

    private RadarAuthorization() {
        // utility class
    }

    /**
     * Similar to {@link RadarToken#hasAuthority(AuthoritiesConstants)}, but this
     * method throws an exception rather than returning a boolean. Useful in combination with e.g.
     * Spring's controllers and exception translators.
     * @param token The token of the requester
     * @param authority The authority to check
     * @throws NotAuthorizedException if the supplied token does not have the authority
     */
    public static void checkAuthority(RadarToken token, AuthoritiesConstants authority)
            throws NotAuthorizedException {
        if (!token.isClientCredentials()) {
            log.debug("Checking authority {} for user {}", authority,
                    token.getUsername());
            if (!token.hasAuthority(authority)) {
                throw new NotAuthorizedException(String.format("Request Client %s does not have "
                        + "authority %s", token.getUsername(), authority));
            }
        }
    }


    /**
     * Similar to {@link RadarToken#hasAuthority(AuthoritiesConstants)}, but this method throws an
     * exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the requester
     * @param authority The authority to check
     * @param permission Permission that the authority should have.
     * @throws NotAuthorizedException if the supplied token does not have the authority
     */
    public static void checkAuthorityAndPermission(RadarToken token, AuthoritiesConstants authority,
            Permission permission)
            throws NotAuthorizedException {
        log.debug("Checking authority {} and permission {} for user {}", authority, permission,
                token.getUsername());
        checkAuthority(token, authority);
        checkPermission(token, permission);
    }

    /**
     * Similar to {@link RadarToken#hasPermission(Permission)}, but this method throws an
     * exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the authenticated client
     * @param permission The permission to check
     * @throws NotAuthorizedException if the supplied token does not have the permission
     */
    public static void checkPermission(RadarToken token, Permission permission)
            throws NotAuthorizedException {
        log.debug("Checking permission {} for user {}", permission.toString(),
                token.getUsername());
        if (!token.hasPermission(permission)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s", token.getUsername(), permission));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnOrganization(Permission, String)},
     * but this method throws an exception rather than returning a boolean.
     * Useful in combination with e.g. Spring's controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param organizationName The organization for which to check the permission
     * @throws NotAuthorizedException if the supplied token
     *      does not have the permission in the given organization
     */
    public static void checkPermissionOnOrganization(RadarToken token, Permission permission,
            String organizationName) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} in organization {}", permission.toString(),
                token.getUsername(), organizationName);
        if (!token.hasPermissionOnOrganization(permission, organizationName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s in organization %s",
                    token.getUsername(), permission, organizationName));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnProject(Permission, String)}, but this method
     * throws an exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project
     */
    public static void checkPermissionOnOrganizationAndProject(RadarToken token,
            Permission permission, String organization, String projectName)
            throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} in organization {} and project {}",
                permission.toString(), token.getUsername(), organization, projectName);
        if (!token.hasPermissionOnOrganizationAndProject(permission, organization, projectName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                            + "permission %s in organization %s and project %s",
                    token.getUsername(), permission, organization,
                    projectName));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnProject(Permission, String)}, but this method
     * throws an exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project
     */
    public static void checkPermissionOnProject(RadarToken token, Permission permission,
            String projectName) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} in project {}", permission.toString(),
                token.getUsername(), projectName);
        if (!token.hasPermissionOnProject(permission, projectName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s in project %s", token.getUsername(), permission,
                    projectName));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnSubject(Permission, String, String)}, but this
     * method throws an exception rather than returning a boolean. Useful in combination with e.g.
     * Spring's controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @param subjectName The name of the subject to check
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project for the given subject
     */
    public static void checkPermissionOnSubject(RadarToken token, Permission permission,
            String projectName, String subjectName) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} on subject {} in project {}",
                permission.toString(), token.getUsername(), subjectName, projectName);
        if (!token.hasPermissionOnSubject(permission, projectName, subjectName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s on subject %s in project %s", token.getUsername(),
                    permission, subjectName, projectName));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnSource(Permission, String, String, String)}, but
     * this method throws an exception rather than returning a boolean. Useful in combination with,
     * e.g., Spring's controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @param subjectName The name of the subject to check
     * @param sourceId The source ID to check
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project for the given subject and source.
     */
    public static void checkPermissionOnSource(RadarToken token, Permission permission,
            String projectName, String subjectName, String sourceId) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} on source {} of subject {} in project {}",
                permission.toString(), token.getUsername(), sourceId, subjectName, projectName);
        if (!token.hasPermissionOnSource(permission, projectName, subjectName, sourceId)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                            + "permission %s on source %s of subject %s in project %s",
                    token.getUsername(), permission, sourceId, subjectName, projectName));
        }
    }
}
