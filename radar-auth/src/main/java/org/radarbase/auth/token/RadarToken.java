package org.radarbase.auth.token;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.authorization.Permission;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

/**
 * Created by dverbeec on 10/01/2018.
 */
public interface RadarToken {
    /**
     * Get all roles defined in this token.
     * @return non-null set describing the roles defined in this token.
     */
    Set<AuthorityReference> getRoles();

    /**
     * Get the global roles (not organization or project-specific) defined in this token.
     * @return non-null map describing the roles defined in this token. The keys in the map are the
     *     project names, and the values are lists of authority names associated to the project.
     */
    default Set<RoleAuthority> getGlobalRoles() {
        return Stream.concat(
                getAuthorities().stream()
                        .map(RoleAuthority::valueOfAuthorityOrNull)
                        .filter(Objects::nonNull),
                getRoles().stream()
                        .map(AuthorityReference::getRole))
                .filter(r -> r.scope() == RoleAuthority.Scope.GLOBAL)
                .collect(toEnumSet());
    }

    /**
     * Get the project roles defined in this token.
     * @return non-null map describing the roles defined in this token. The keys in the map are the
     *     project names, and the values are lists of authority names associated to the project.
     */
    default Map<String, Set<RoleAuthority>> getProjectRoles() {
        return getRoles().stream()
                .filter(r -> r.getRole().scope() == RoleAuthority.Scope.PROJECT
                        && r.getReferent() != null)
                .collect(groupingByReferent());
    }

    /**
     * Get the organization roles defined in this token.
     * @return non-null map describing the roles defined in this token. The keys in the map are the
     *     project names, and the values are lists of authority names associated to the project.
     */
    default Map<String, Set<RoleAuthority>> getOrganizationRoles() {
        return getRoles().stream()
                .filter(r -> r.getRole().scope() == RoleAuthority.Scope.ORGANIZATION
                        && r.getReferent() != null)
                .collect(groupingByReferent());
    }

    /**
     * Get the referents (i.e., organization or project name) in a given scope that matches a
     * permission.
     * @param scope scope of the referents
     * @param permission permission that should be authorized within the scope
     * @return referents names
     */
    default Stream<String> getReferentsWithPermission(RoleAuthority.Scope scope,
            Permission permission) {
        return getRoles().stream()
                .filter(r -> r.getRole().scope() == scope
                        && permission.isRoleAllowed(r.getRole()))
                .map(AuthorityReference::getReferent)
                .filter(Objects::nonNull);
    }

    /**
     * Check if any non-project related authority has the given permission. Currently the only
     * non-project authority is {@code SYS_ADMIN}, so we only check for that.
     * @param permission the permission
     * @return {@code true} if any non-project related authority has the permission, {@code false}
     *     otherwise
     */
    default boolean hasGlobalAuthorityForPermission(Permission permission) {
        return Stream.concat(
                        getAuthorities().stream().map(RoleAuthority::valueOfAuthorityOrNull),
                        getRoles().stream().map(AuthorityReference::getRole))
                .anyMatch(r -> r != null
                        && r.scope() == RoleAuthority.Scope.GLOBAL
                        && permission.isRoleAllowed(r));
    }

    /**
     * Get a list of non-project related authorities.
     * @return non-null list of authority names
     */
    List<String> getAuthorities();

    /**
     * Get a list of non-project related authorities.
     * @return non-null list of authority names
     */
    default Set<RoleAuthority> getRoleAuthorities() {
        return getAuthorities().stream()
                .map(RoleAuthority::valueOfAuthorityOrNull)
                .filter(Objects::nonNull)
                .collect(toEnumSet());
    }

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
     * Get the token username.
     */
    String getUsername();

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
     * <p>This token <strong>must</strong> have the authority in its set of authorities. If it's a
     * client credentials token, this is the only requirement, as a client credentials token is
     * linked to an OAuth client and not to a user in the system.
     * @param authority The permission to check
     * @return {@code true} if this token has the permission, {@code false} otherwise
     */
    boolean hasAuthority(RoleAuthority authority);

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
     * Check if this token gives the given permission from a global scope.
     *
     * <p>This token <strong>must</strong> have the permission in its set of scopes. If it's a
     * client credentials token, this is the only requirement, as a client credentials token is
     * linked to an OAuth client and not to a user in the system. If it's not a client
     * credentials token, this also checks to see if the user has a global role with the specified
     * permission.</p>
     * @param permission The permission to check
     * @return {@code true} if this token has the permission, {@code false} otherwise
     */
    boolean hasGlobalPermission(Permission permission);

    /**
     * Check if this token gives a permission in a specific organization.
     * @param permission the permission
     * @param organization the organization name
     * @return true if this token has the permission in the project, false otherwise
     */
    boolean hasPermissionOnOrganization(Permission permission, String organization);

    /**
     * Check if this token gives a permission in a specific project in a given organization.
     * @param permission the permission
     * @param organization the organization name
     * @param projectName the project name
     * @return true if this token has the permission in the project, false otherwise
     */
    boolean hasPermissionOnOrganizationAndProject(Permission permission, String organization,
            String projectName);

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

    /**
     * Whether the current credentials were obtained with a OAuth 2.0 client credentials flow.
     *
     * @return true if the client credentials flow was certainly used, false otherwise.
     */
    boolean isClientCredentials();

    private static Collector<RoleAuthority, ?, Set<RoleAuthority>> toEnumSet() {
        return toCollection(() -> EnumSet.noneOf(RoleAuthority.class));
    }

    private static Collector<AuthorityReference, ?, Map<String, Set<RoleAuthority>>>
            groupingByReferent() {
        return groupingBy(AuthorityReference::getReferent,
                mapping(AuthorityReference::getRole, toEnumSet()));
    }
}
