package org.radarcns.auth.token;

import java.util.Collection;
import java.util.Objects;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.authorization.Permission;

import java.util.Collections;

/**
 * Partial implementation of {@link RadarToken}, providing a default implementation for the three
 * permission checks.
 */
public abstract class AbstractRadarToken implements RadarToken {

    protected static final String CLIENT_CREDENTIALS = "client_credentials";

    @Override
    public boolean hasPermission(Permission permission) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForPermission(permission));

    }

    @Override
    public boolean hasPermissionOnProject(Permission permission, String projectName) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForPermission(permission, projectName));
    }

    @Override
    public boolean hasPermissionOnSubject(Permission permission, String projectName,
            String subjectName) {
        return hasScope(permission.scopeName())
                && (isClientCredentials() || hasAuthorityForPermission(permission, projectName,
                        subjectName));
    }

    protected boolean hasScope(String scope) {
        return getScopes().contains(scope);
    }

    protected boolean isClientCredentials() {
        return CLIENT_CREDENTIALS.equals(getGrantType());
    }

    /**
     * Check all authorities in this token, project and non-project specific, for the given
     * permission.
     * @param permission the permission
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForPermission(Permission permission) {
        return getRoles().values().stream()
                .flatMap(Collection::stream)
                .anyMatch(permission::isAuthorityAllowed)
                || hasNonProjectRelatedAuthorityForPermission(permission);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission.
     * @param permission the permission
     * @param projectName the project name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForPermission(Permission permission, String projectName) {
        return getRoles().getOrDefault(projectName, Collections.emptyList()).stream()
                .anyMatch(permission::isAuthorityAllowed)
                || hasNonProjectRelatedAuthorityForPermission(permission);
    }

    /**
     * Check authorities in this token linked to the given project, or not linked to any project
     * (such as {@code SYS_ADMIN}), for the given permission on the given subject.
     * @param permission the permission
     * @param projectName the project name
     * @param subjectName the subject name
     * @return {@code true} if any authority contains the permission, {@code false} otherwise
     */
    protected boolean hasAuthorityForPermission(Permission permission, String projectName,
            String subjectName) {
        // if we're only a participant, we can only do operations on our own data
        return (isJustParticipant(projectName) && getSubject().equals(subjectName)
                && permission.isAuthorityAllowed(AuthoritiesConstants.PARTICIPANT))
                // if we have other roles beside participant, we should check those on the
                // project level
                || (!isJustParticipant(projectName) && hasAuthorityForPermission(permission,
                        projectName));
    }

    /**
     * Check if any non-project related authority has the given permission. Currently the only
     * non-project authority is {@code SYS_ADMIN}, so we only check for that.
     * @param permission the permission
     * @return {@code true} if any non-project related authority has the permission, {@code false}
     *     otherwise
     */
    protected boolean hasNonProjectRelatedAuthorityForPermission(Permission permission) {
        return getAuthorities().contains(AuthoritiesConstants.SYS_ADMIN)
                && permission.isAuthorityAllowed(AuthoritiesConstants.SYS_ADMIN);
    }

    /**
     * Check if this token only has the participant role in the given project.
     * @param projectName the project
     * @return {@code true} if this token only has the participant role in the given project,
     *     {@code false} otherwise
     */
    protected boolean isJustParticipant(String projectName) {
        return Objects.equals(getRoles().get(projectName),
            Collections.singletonList(AuthoritiesConstants.PARTICIPANT));
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
            + ", token='" + getToken() + '\''
            + '}';
    }
}
