package org.radarbase.auth.token;

import org.radarbase.auth.authorization.AuthoritiesConstants;
import org.radarbase.auth.authorization.Permission;

import java.util.Objects;
import java.util.Set;

import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.SYS_ADMIN;

/**
 * Partial implementation of {@link RadarToken}, providing a default implementation for the three
 * permission checks.
 */
public abstract class AbstractRadarToken implements RadarToken {
    protected static final String CLIENT_CREDENTIALS = "client_credentials";

    @Override
    public boolean hasAuthority(AuthoritiesConstants authority) {
        return isClientCredentials()
                || getAuthoritiesConstants().stream().anyMatch(authority::equals);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForPermission(permission));

    }

    @Override
    public boolean hasPermissionOnOrganization(Permission permission, String organization) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForOrganization(permission, organization));
    }

    @Override
    public boolean hasPermissionOnOrganizationAndProject(Permission permission, String organization,
            String projectName) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForOrganizationAndProject(
                permission, organization, projectName));
    }

    @Override
    public boolean hasPermissionOnProject(Permission permission, String projectName) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForProject(
                        permission, projectName));
    }

    @Override
    public boolean hasPermissionOnSubject(Permission permission, String projectName,
            String subjectName) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForSubject(permission, projectName,
                        subjectName));
    }

    @Override
    public boolean hasPermissionOnSource(Permission permission, String projectName,
            String subjectName, String sourceId) {
        return hasScope(permission.scopeName())
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
        return getRoles().stream()
                .map(AuthorityReference::getRole)
                .anyMatch(permission::isAuthorityAllowed);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param organization the organization name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForOrganization(Permission permission, String organization) {
        if (hasNonProjectRelatedAuthorityForPermission(permission)) {
            return true;
        }
        if (organization == null) {
            return false;
        }
        Set<AuthoritiesConstants> organizationRoles = getOrganizationRoles().get(organization);
        return organizationRoles != null
                && organizationRoles.stream().anyMatch(permission::isAuthorityAllowed);
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
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param projectName the project name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForProject(Permission permission, String projectName) {
        if (hasNonProjectRelatedAuthorityForPermission(permission)) {
            return true;
        }
        if (projectName == null) {
            return false;
        }
        Set<AuthoritiesConstants> projectRoles = getProjectRoles().get(projectName);
        return projectRoles != null
                && projectRoles.stream().anyMatch(permission::isAuthorityAllowed);
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
        if (isJustParticipant(projectName)) {
            // if we're only a participant, we can only do operations on our own data
            return getSubject().equals(subjectName)
                    && permission.isAuthorityAllowed(PARTICIPANT);
        } else {
            // if we have other roles beside participant, we should check those on the
            // project level
            return hasAuthorityForProject(permission, projectName);
        }
    }

    protected boolean hasAuthorityForSource(Permission permission, String projectName,
            String subjectName, String sourceId) {
        if (!hasAuthorityForSubject(permission, projectName, subjectName)) {
            return false;
        }
        if (isJustParticipant(projectName)) {
            return getSources().contains(sourceId);
        } else {
            return true;
        }
    }

    /**
     * Check if any non-project related authority has the given permission. Currently the only
     * non-project authority is {@code SYS_ADMIN}, so we only check for that.
     * @param permission the permission
     * @return {@code true} if any non-project related authority has the permission, {@code false}
     *     otherwise
     */
    protected boolean hasNonProjectRelatedAuthorityForPermission(Permission permission) {
        return getAuthoritiesConstants().contains(SYS_ADMIN)
                && permission.isAuthorityAllowed(SYS_ADMIN);
    }

    /**
     * Check if this token only has the participant role in the given project.
     * @param projectName the project
     * @return {@code true} if this token only has the participant role in the given project,
     *     {@code false} otherwise
     */
    protected boolean isJustParticipant(String projectName) {
        return Set.of(PARTICIPANT).equals(getProjectRoles().get(projectName));
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
