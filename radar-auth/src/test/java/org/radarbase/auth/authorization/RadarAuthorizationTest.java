package org.radarbase.auth.authorization;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.radarbase.auth.authorization.Permission.Entity;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.JwtRadarToken;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.auth.util.TokenTestUtils;

/**
 * Created by dverbeec on 25/09/2017.
 */
public class RadarAuthorizationTest {

    @BeforeAll
    public static void loadToken() throws Exception {
        TokenTestUtils.setUp();
    }

    @Test
    public void testCheckPermissionOnProject() throws NotAuthorizedException {
        String project = "PROJECT1";
        // let's get all permissions a project admin has
        Set<Permission> permissions = Permissions.getPermissionMatrix().entrySet().stream()
                .filter(e -> e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        for (Permission p : permissions) {
            RadarAuthorization.checkPermissionOnProject(token, p, project);
        }

        Set<Permission> notPermitted = Permissions.getPermissionMatrix().entrySet().stream()
                .filter(e -> !e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        notPermitted.forEach(p -> assertNotAuthorized(() ->
                RadarAuthorization.checkPermissionOnProject(token, p, project),
                String.format("Token should not have permission %s on project %s", p, project)));
    }

    @Test
    public void testCheckPermission() throws NotAuthorizedException {
        RadarToken token = new JwtRadarToken(TokenTestUtils.SUPER_USER_TOKEN);
        for (Permission p : Permission.allPermissions()) {
            RadarAuthorization.checkPermission(token, p);
        }
    }

    @Test
    public void testCheckPermissionOnSelf() throws NotAuthorizedException {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = token.getSubject();
        for (Permission p : Arrays.asList(Permission.MEASUREMENT_CREATE,
                Permission.MEASUREMENT_READ, Permission.SUBJECT_UPDATE, Permission.SUBJECT_READ)) {
            RadarAuthorization.checkPermissionOnSubject(token, p, project, subject);
        }
    }

    @Test
    public void testCheckPermissionOnOtherSubject() {
        // is only participant in project2, so should not have any permission on another subject
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String other = "other-subject";
        Permission.allPermissions()
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnSubject(token, p, project, other),
                    "Token should not have permission " + p + " on another subject"));
    }

    @Test
    public void testCheckPermissionOnSubject() throws NotAuthorizedException {
        // project admin should have all permissions on subject in his project
        String project = "PROJECT1";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = "some-subject";
        Set<Permission> permissions = Permission.allPermissions().stream()
                .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
                .collect(Collectors.toSet());
        for (Permission p : permissions) {
            RadarAuthorization.checkPermissionOnSubject(token, p, project, subject);
        }
    }

    @Test
    public void testMultipleRolesInProjectToken() throws NotAuthorizedException {
        String project = "PROJECT2";
        RadarToken token = new JwtRadarToken(TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN);
        String subject = "some-subject";
        Set<Permission> permissions = Permission.allPermissions().stream()
                .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
                .collect(Collectors.toSet());
        for (Permission p : permissions) {
            RadarAuthorization.checkPermissionOnSubject(token, p, project, subject);
        }
    }

    @Test
    public void testCheckPermissionOnSource() throws NotAuthorizedException {
        String project = "PROJECT1";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = "some-subject";
        String source = "source-1";

        Permission.allPermissions()
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnSource(
                                token, p, project, subject, source),
                        "Token should not have permission " + p + " on another subject"));
    }

    @Test
    public void testCheckPermissionOnOwnSource() throws NotAuthorizedException {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN);
        String subject = token.getSubject();
        String source = "source-1";  // source to use

        Set<Permission> permissions = Permission.allPermissions().stream()
                .filter(p -> p.getEntity() == Entity.MEASUREMENT)
                .collect(Collectors.toSet());

        for (Permission p : permissions) {
            RadarAuthorization.checkPermissionOnSource(token, p, project, subject, source);
        }
    }


    @Test
    public void testCheckPermissionOnOtherSource() {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN);
        String subject = token.getSubject();
        String source = "source-2";  // source to use

        Permission.allPermissions()
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnSource(
                                token, p, project, subject, source),
                        "Token should not have permission " + p + " on another subject"));
    }

    @Test
    public void testScopeOnlyToken() throws NotAuthorizedException {
        RadarToken token = new JwtRadarToken(TokenTestUtils.SCOPE_TOKEN);
        // test that we can do the things we have a scope for
        Collection<Permission> scope = Arrays.asList(
                Permission.SUBJECT_READ, Permission.SUBJECT_CREATE, Permission.PROJECT_READ,
                Permission.MEASUREMENT_CREATE);

        for (Permission p : scope) {
            RadarAuthorization.checkPermission(token, p);
            RadarAuthorization.checkPermissionOnProject(token, p, "");
            RadarAuthorization.checkPermissionOnSubject(token, p, "", "");
            RadarAuthorization.checkPermissionOnSource(token, p, "", "", "");
        }

        // test we can do nothing else, for each of the checkPermission methods
        Permission.allPermissions().stream()
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermission(token, p),
                    "Permission " + p + " is granted but not in scope."));

        Permission.allPermissions().stream()
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnProject(token, p, ""),
                    "Permission " + p + " is granted but not in scope."));

        Permission.allPermissions().stream()
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnSubject(token, p, "", ""),
                    "Permission " + p + " is granted but not in scope."));

        Permission.allPermissions().stream()
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> RadarAuthorization.checkPermissionOnSource(token, p, "", "", ""),
                        "Permission " + p + " is granted but not in scope."));
    }

    private static void assertNotAuthorized(AuthorizationCheck supplier, String message) {
        try {
            supplier.check();
            fail(message);
        } catch (GeneralSecurityException e) {
            assertThat(e, instanceOf(NotAuthorizedException.class));
        }
    }

    @FunctionalInterface
    interface AuthorizationCheck {
        void check() throws GeneralSecurityException;
    }
}
