package org.radarcns.auth.unit.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.authorization.Permissions;
import org.radarcns.auth.authorization.RadarAuthorization;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.auth.unit.util.TokenTestUtils;

/**
 * Created by dverbeec on 25/09/2017.
 */
public class RadarAuthorizationTest {

    @BeforeClass
    public static void loadToken() throws Exception {
        TokenTestUtils.setUp();
    }

    @Test
    public void testCheckPermissionOnProject() {
        String project = "PROJECT1";
        // let's get all permissions a project admin has
        Set<Permission> permissions = Permissions.getPermissionMatrix().entrySet().stream()
                .filter(e -> e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
        DecodedJWT token = TokenTestUtils.PROJECT_ADMIN_TOKEN;
        permissions.stream()
            .forEach(p -> RadarAuthorization.checkPermissionOnProject(token, p, project));

        Set<Permission> notPermitted = Permissions.getPermissionMatrix().entrySet().stream()
                .filter(e -> !e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());

        notPermitted.stream()
                .forEach(p -> {
                    try {
                        RadarAuthorization.checkPermissionOnProject(token, p, project);
                    } catch (NotAuthorizedException ex) {
                        return;
                    }
                    fail();
                });
    }

    @Test
    public void testCheckPermission() {
        DecodedJWT token = TokenTestUtils.SUPER_USER_TOKEN;
        Permission.allPermissions().stream()
            .forEach(p -> RadarAuthorization.checkPermission(token, p));
    }

    @Test
    public void testCheckPermissionOnSelf() {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        DecodedJWT token = TokenTestUtils.PROJECT_ADMIN_TOKEN;
        String subject = token.getSubject();
        Arrays.asList(Permission.MEASUREMENT_CREATE, Permission.MEASUREMENT_READ,
                Permission.SUBJECT_UPDATE, Permission.SUBJECT_READ).stream()
                        .forEach(p -> RadarAuthorization.checkPermissionOnSubject(
                                token, p, project, subject));
    }

    @Test
    public void testCheckPermissionOnOtherSubject() {
        // is only participant in project2, so should not have any permission on another subject
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        DecodedJWT token = TokenTestUtils.PROJECT_ADMIN_TOKEN;
        String other = "other-subject";
        Permission.allPermissions().stream()
                .forEach(p -> {
                    try {
                        RadarAuthorization.checkPermissionOnSubject(token, p, project, other);
                    } catch (NotAuthorizedException ex) {
                        return;
                    }
                    fail("Permission " + p.toString() + " is allowed");
                });
    }

    @Test
    public void testCheckPermissionOnSubject() {
        // project admin should have all permissions on subject in his project
        String project = "PROJECT1";
        // this token is participant in PROJECT2
        DecodedJWT token = TokenTestUtils.PROJECT_ADMIN_TOKEN;
        String subject = "some-subject";
        Permission.allPermissions().stream()
            .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
            .forEach(p -> RadarAuthorization.checkPermissionOnSubject(token, p, project, subject));
    }

    @Test
    public void testMultipleRolesInProjectToken() {
        String project = "PROJECT2";
        DecodedJWT token = TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN;
        String subject = "some-subject";
        Permission.allPermissions().stream()
            .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
            .forEach(p -> RadarAuthorization.checkPermissionOnSubject(token, p, project, subject));
    }

    @Test
    public void testScopeOnlyToken() {
        DecodedJWT token = TokenTestUtils.SCOPE_TOKEN;
        // test that we can do the things we have a scope for
        Arrays.asList(Permission.SUBJECT_READ, Permission.SUBJECT_CREATE, Permission.PROJECT_READ)
                .stream()
                .forEach(p -> {
                    RadarAuthorization.checkPermission(token, p);
                    RadarAuthorization.checkPermissionOnProject(token, p, "");
                    RadarAuthorization.checkPermissionOnSubject(token, p, "", "");
                });

        // test we can do nothing else, for each of the checkPermission methods
        Permission.allPermissions().stream()
                .filter(p -> !(p.equals(Permission.SUBJECT_READ)
                        || p.equals(Permission.SUBJECT_CREATE)
                        || p.equals(Permission.PROJECT_READ)))
                .forEach(p -> {
                    try {
                        RadarAuthorization.checkPermission(token, p);
                    } catch (NotAuthorizedException ex) {
                        return;
                    }
                    fail();
                });

        Permission.allPermissions().stream()
                .filter(p -> !(p.equals(Permission.SUBJECT_READ)
                        || p.equals(Permission.SUBJECT_CREATE)
                        || p.equals(Permission.PROJECT_READ)))
                .forEach(p -> {
                    try {
                        RadarAuthorization.checkPermissionOnProject(token, p, "");
                    } catch (NotAuthorizedException ex) {
                        return;
                    }
                    fail();
                });

        Permission.allPermissions().stream()
                .filter(p -> !(p.equals(Permission.SUBJECT_READ)
                        || p.equals(Permission.SUBJECT_CREATE)
                        || p.equals(Permission.PROJECT_READ)))
                .forEach(p -> {
                    try {
                        RadarAuthorization.checkPermissionOnSubject(token, p, "", "");
                    } catch (NotAuthorizedException ex) {
                        return;
                    }
                    fail();
                });

        assertFalse(RadarAuthorization.isJustParticipant(token, ""));
    }

}
