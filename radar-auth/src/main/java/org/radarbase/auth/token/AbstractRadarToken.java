package org.radarbase.auth.token;

import org.radarbase.auth.authorization.Permission;
import org.radarbase.auth.authorization.RoleAuthority;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Partial implementation of {@link RadarToken}, providing a default implementation for the three
 * permission checks.
 */
public abstract class AbstractRadarToken implements RadarToken {
    protected static final String CLIENT_CREDENTIALS = "client_credentials";

    @Override
    public boolean hasAuthority(RoleAuthority authority) {
        return isClientCredentials()
                || getRoleAuthorities().stream().anyMatch(authority::equals);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasAuthorityForPermission(permission));

    }

    @Override
    public boolean hasGlobalPermission(Permission permission) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasGlobalAuthorityForPermission(permission));
    }

    @Override
    public boolean hasPermissionOnOrganization(Permission permission, String organization) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasAuthorityForOrganization(permission, organization));
    }

    @Override
    public boolean hasPermissionOnOrganizationAndProject(Permission permission, String organization,
            String projectName) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasAuthorityForOrganizationAndProject(
                permission, organization, projectName));
    }

    @Override
    public boolean hasPermissionOnProject(Permission permission, String projectName) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasAuthorityForProject(
                        permission, projectName));
    }

    @Override
    public boolean hasPermissionOnSubject(Permission permission, String projectName,
            String subjectName) {
        return hasScope(permission.scope())
                && (isClientCredentials() || hasAuthorityForSubject(permission, projectName,
                        subjectName));
    }

    @Override
    public boolean hasPermissionOnSource(Permission permission, String projectName,
            String subjectName, String sourceId) {
        return hasScope(permission.scope())
                && (isClientCredentials()
                || hasAuthorityForSource(permission, projectName, subjectName, sourceId));
    }

    protected boolean hasScope(String scope) {
        return getScopes().contains(scope);
    }

    @Override
    public boolean isClientCredentials() {
        return CLIENT_CREDENTIALS.equals(getGrantType());
    }

    /**
     * Check all authorities in this token, project and non-project specific, for the given
     * permission.
     * @param permission the permission
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForPermission(Permission permission) {
        return Stream.concat(getRoles().stream().map(AuthorityReference::getRole),
                        getRoleAuthorities().stream())
                .anyMatch(permission::isRoleAllowed);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param organization the organization name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForOrganization(Permission permission, String organization) {
        if (hasGlobalAuthorityForPermission(permission)) {
            return true;
        }
        if (organization == null) {
            return false;
        }
        return getReferentsWithPermission(RoleAuthority.Scope.ORGANIZATION, permission)
                .anyMatch(organization::equals);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param organization the organization name
     * @param projectName the project name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForOrganizationAndProject(Permission permission,
            String organization, String projectName) {
        return hasAuthorityForOrganization(permission, organization)
                || hasAuthorityForProject(permission, projectName);
    }

    /**
     * Check whether roles from this token give authority to a given subject. By providing an
     * additional personal role condition, if the authority is personal (e.g. subject-specific),
     * the predicate for that subject will be evaluated.
     *
     * @param permission permission to check.
     * @param projectName project name.
     * @param personalRoleCondition additional condition (possibly null) when a role is
     *                              only applies to the person.
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForProject(
            Permission permission,
            String projectName,
            Predicate<RoleAuthority> personalRoleCondition) {
        if (hasGlobalAuthorityForPermission(permission)) {
            return true;
        }
        if (projectName == null) {
            return false;
        }
        return getAuthorityReferencesWithPermission(RoleAuthority.Scope.PROJECT, permission)
                .anyMatch(r -> projectName.equals(r.getReferent())
                        && (personalRoleCondition == null
                        || !r.getRole().isPersonal()
                        || personalRoleCondition.test(r.getRole())));
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param projectName the project name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForProject(Permission permission, String projectName) {
        return hasAuthorityForProject(permission, projectName,
                p -> permission.getOperation() == Permission.Operation.READ);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission on the given subject.
     * @param permission the permission
     * @param projectName the project name
     * @param subjectName the subject name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForSubject(Permission permission, String projectName,
            String subjectName) {
        return hasAuthorityForProject(permission, projectName, role ->
                getUsername().equals(subjectName));
    }

    protected boolean hasAuthorityForSource(Permission permission, String projectName,
            String subjectName, String sourceId) {
        return hasAuthorityForProject(permission, projectName, role ->
                getUsername().equals(subjectName) && getSources().contains(sourceId));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        return Objects.equals(getToken(), ((AbstractRadarToken)other).getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getToken());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
            + "scopes=" + getScopes()
            + ", username='" + getUsername() + '\''
            + ", subject='" + getSubject() + '\''
            + ", roles=" + getRoles()
            + ", sources=" + getSources()
            + ", authorities=" + getAuthorities()
            + ", grantType='" + getGrantType() + '\''
            + ", audience=" + getAudience()
            + ", issuer='" + getIssuer() + '\''
            + ", issuedAt=" + getIssuedAt()
            + ", expiresAt=" + getExpiresAt()
            + ", type='" + getType() + '\''
            + '}';
    }

}
