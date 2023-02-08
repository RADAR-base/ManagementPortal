package org.radarbase.auth.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.radarbase.auth.authorization.Permission.Entity;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.jwt.JwtRadarToken;
import org.radarbase.auth.token.AbstractRadarTokenTest;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.auth.util.TokenTestUtils;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by dverbeec on 25/09/2017.
 */
class RadarAuthorizationTest {
    private AuthorizationOracle oracle;

    @BeforeEach
    public void setUp() {
        this.oracle = new AuthorizationOracle(
                new AbstractRadarTokenTest.MockEntityRelationService());
    }

    @Test
    void testCheckPermissionOnProject() throws NotAuthorizedException {
        String project = "PROJECT1";
        // let's get all permissions a project admin has
        Set<Permission> permissions = AuthorizationOracle.Permissions.getPermissionMatrix()
                .entrySet().stream()
                .filter(e -> e.getValue().contains(RoleAuthority.PROJECT_ADMIN))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        for (Permission p : permissions) {
            oracle.checkPermission(token, p, e -> e.project(project));
        }

        Set<Permission> notPermitted = AuthorizationOracle.Permissions.getPermissionMatrix()
                .entrySet().stream()
                .filter(e -> !e.getValue().contains(RoleAuthority.PROJECT_ADMIN))
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        notPermitted.forEach(p -> assertNotAuthorized(() ->
                oracle.checkPermission(token, p, e -> e.project(project)),
                String.format("Token should not have permission %s on project %s", p, project)));
    }

    @Test
    void testCheckPermissionOnOrganization() throws NotAuthorizedException {
        JwtRadarToken token = new JwtRadarToken(TokenTestUtils.ORGANIZATION_ADMIN_TOKEN);
        assertNotAuthorized(() -> oracle.checkPermission(token, Permission.ORGANIZATION_CREATE,
                e -> e.organization("main")),
                "Token should not be able to create organization");
        oracle.checkPermission(token, Permission.PROJECT_CREATE, e -> e.organization("main"));
        oracle.checkPermission(token, Permission.SUBJECT_CREATE,
                e -> e.organization("main").project("PROJECT1"));
        assertNotAuthorized(() -> oracle.checkPermission(token, Permission.PROJECT_CREATE,
                        e -> e.organization("other")),
                "Token should not be able to create project in other organization");
        assertNotAuthorized(() -> oracle.checkPermission(
                        token, Permission.SUBJECT_CREATE,
                        e -> e.organization("other").project("PROJECT1")),
                "Token should not be able to create subject in other organization");
    }

    @Test
    void testCheckPermission() throws NotAuthorizedException {
        RadarToken token = new JwtRadarToken(TokenTestUtils.SUPER_USER_TOKEN);
        for (Permission p : Permission.values()) {
            oracle.checkPermission(token, p);
        }
    }

    @Test
    void testCheckPermissionOnSelf() throws NotAuthorizedException {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = token.getSubject();
        for (Permission p : Arrays.asList(Permission.MEASUREMENT_CREATE,
                Permission.MEASUREMENT_READ, Permission.SUBJECT_UPDATE, Permission.SUBJECT_READ)) {
            oracle.checkPermission(token, p, e -> e.project(project).subject(subject));
        }
    }

    @Test
    void testCheckPermissionOnOtherSubject() {
        // is only participant in project2, so should not have any permission on another subject
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String other = "other-subject";
        Stream.of(Permission.values())
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(token, p, e -> e.project(project).subject(other)),
                    "Token should not have permission " + p + " on another subject"));
    }

    @Test
    void testCheckPermissionOnSubject() throws NotAuthorizedException {
        // project admin should have all permissions on subject in his project
        String project = "PROJECT1";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = "some-subject";
        Set<Permission> permissions = Stream.of(Permission.values())
                .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
                .collect(Collectors.toSet());
        for (Permission p : permissions) {
            oracle.checkPermission(token, p, e -> e.project(project).subject(subject));
        }
    }

    @Test
    void testMultipleRolesInProjectToken() throws NotAuthorizedException {
        String project = "PROJECT2";
        RadarToken token = new JwtRadarToken(TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN);
        String subject = "some-subject";
        Set<Permission> permissions = Stream.of(Permission.values())
                .filter(p -> p.getEntity() == Permission.Entity.SUBJECT)
                .collect(Collectors.toSet());
        for (Permission p : permissions) {
            oracle.checkPermission(token, p, e -> e.project(project).subject(subject));
        }
    }

    @Test
    void testCheckPermissionOnSource() {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.PROJECT_ADMIN_TOKEN);
        String subject = "some-subject";
        String source = "source-1";

        Stream.of(Permission.values())
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(
                                token, p, e -> e.project(project).subject(subject).source(source)),
                        "Token should not have permission " + p + " on another subject"));
    }

    @Test
    void testCheckPermissionOnOwnSource() throws NotAuthorizedException {
        String project = "PROJECT2";
        // this token is participant in PROJECT2
        RadarToken token = new JwtRadarToken(TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN);
        String subject = token.getSubject();
        String source = "source-1";  // source to use

        Set<Permission> permissions = Stream.of(Permission.values())
                .filter(p -> p.getEntity() == Entity.MEASUREMENT)
                .collect(Collectors.toSet());

        for (Permission p : permissions) {
            oracle.checkPermission(
                    token, p, e -> e.project(project).subject(subject).source(source));
        }
    }

    @Test
    void testScopeOnlyToken() throws NotAuthorizedException {
        RadarToken token = new JwtRadarToken(TokenTestUtils.SCOPE_TOKEN);
        // test that we can do the things we have a scope for
        Collection<Permission> scope = Arrays.asList(
                Permission.SUBJECT_READ, Permission.SUBJECT_CREATE, Permission.PROJECT_READ,
                Permission.MEASUREMENT_CREATE);

        for (Permission p : scope) {
            oracle.checkPermission(token, p);
            oracle.checkPermission(token, p, e -> e.project(""));
            oracle.checkPermission(token, p, e -> e.project("").subject(""));
            oracle.checkPermission(token, p, e -> e.project("").subject("").source(""));
        }

        // test we can do nothing else, for each of the checkPermission methods
        Stream.of(Permission.values())
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(token, p),
                    "Permission " + p + " is granted but not in scope."));

        Stream.of(Permission.values())
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(token, p, e -> e.project("")),
                    "Permission " + p + " is granted but not in scope."));

        Stream.of(Permission.values())
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(token, p, e -> e.project("").subject("")),
                    "Permission " + p + " is granted but not in scope."));

        Stream.of(Permission.values())
                .filter(p -> !scope.contains(p))
                .forEach(p -> assertNotAuthorized(
                    () -> oracle.checkPermission(token, p,
                            e -> e.project("").subject("").source("")),
                        "Permission " + p + " is granted but not in scope."));
    }

    private static void assertNotAuthorized(AuthorizationCheck supplier, String message) {
        assertThrows(NotAuthorizedException.class, supplier::check, message);
    }

    @FunctionalInterface
    interface AuthorizationCheck {
        void check() throws GeneralSecurityException;
    }
}
