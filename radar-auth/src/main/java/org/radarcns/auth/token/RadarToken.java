package org.radarcns.auth.token;

import org.radarcns.auth.authorization.Permission;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dverbeec on 10/01/2018.
 */
public interface RadarToken {

    /**
     * Get the roles defined in this token.
     * @return non-null map describing the roles defined in this token. The keys in the map are the
     *     project names, and the values are lists of authority names associated to the project.
     */
    Map<String, List<String>> getRoles();

    /**
     * Get a list of non-project related authorities.
     * @return non-null list of authority names
     */
    List<String> getAuthorities();

    /**
     * Get a list of scopes assigned to this token.
     * @return non-null list of scope names
     */
    List<String> getScopes();

    /**
     * Get a list of source names associated with this token.
     * @return non-null list of source names
     */
    List<String> getSources();

    /**
     * Get this token's OAuth2 grant type.
     * @return non-null grant type
     */
    String getGrantType();

    /**
     * Get the token subject.
     * @return non-null subject
     */
    String getSubject();

    /**
     * Get the date this token was issued.
     * @return date this token was issued or null
     */
    Date getIssuedAt();

    /**
     * Get the date this token expires.
     * @return date this token expires or null
     */
    Date getExpiresAt();

    /**
     * Get the audience of the token.
     * @return non-null list of resources that are allowed to accept the token
     */
    List<String> getAudience();

    /**
     * Get a string representation of this token.
     * @return non-null string representation of this token
     */
    String getToken();

    /**
     * Get the issuer.
     * @return non-null issuer
     */
    String getIssuer();

    /**
     * Get the token type.
     * @return non-null token type.
     */
    String getType();

    /**
     * Client that the token is associated to.
     * @return client ID if set or null if unknown.
     */
    String getClientId();

    /**
     * Get a token claim by name.
     * @param name claim name.
     * @return a claim value or null if none was found or the type was not a string.
     */
    String getClaimString(String name);

    /**
     * Get a token claim list by name.
     * @param name claim name.
     * @return a claim list of values or null if none was found or the type was not a string.
     */
    List<String> getClaimList(String name);

    /**
     * Check if this token gives the given permission, not taking into account project affiliations.
     *
     * <p>This token <strong>must</strong> have the permission in its set of scopes. If it's a
     * client credentials token, this is the only requirement, as a client credentials token is
     * linked to an OAuth client and not to a user in the system. If it's not a client
     * credentials token, this also checks to see if the user has a role with the specified
     * permission.</p>
     * @param permission The permission to check
     * @return {@code true} if this token has the permission, {@code false} otherwise
     */
    boolean hasPermission(Permission permission);

    /**
     * Check if this token gives a permission in a specific project.
     * @param permission the permission
     * @param projectName the project name
     * @return true if this token has the permission in the project, false otherwise
     */
    boolean hasPermissionOnProject(Permission permission, String projectName);

    /**
     * Check if this token gives a permission on a subject in a given project.
     * @param permission the permission
     * @param projectName the project name
     * @param subjectName the subject name
     * @return true if this token ahs the permission for the subject in the given project, false
     *     otherwise
     */
    boolean hasPermissionOnSubject(Permission permission, String projectName, String subjectName);

    /**
     * Check if this token gives a permission on a given source.
     * @param permission the permission
     * @param projectName the project name
     * @param subjectName the subject name
     * @param sourceId the source ID
     * @return true if this token gives permission for the source, false otherwise
     */
    boolean hasPermissionOnSource(Permission permission, String projectName, String subjectName,
            String sourceId);
}
