package org.radarcns.auth.unit.authorization;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.authorization.Permissions;
import org.radarcns.auth.authorization.RadarAuthorization;
import org.radarcns.auth.unit.util.TokenTestUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dverbeec on 25/09/2017.
 */
public class RadarAuthorizationTest {

    @BeforeClass
    public static void loadToken() throws Exception {
        TokenTestUtils.setUp();
    }

    @Test
    public void testHasPermissionInProject() {
        String project = "PROJECT1";
        // let's get all permissions a project admin has
        Set<Permission> permissions = Permissions.getPermissionMatrix().entrySet().stream()
            .filter(e -> e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
            .map(e -> e.getKey())
            .collect(Collectors.toSet());
        DecodedJWT token = TokenTestUtils.PROJECT_ADMIN_TOKEN;
        permissions.stream()
            .forEach(p -> assertTrue(RadarAuthorization.hasPermissionInProject(token, p, project)));

        Set<Permission> notPermitted = Permissions.getPermissionMatrix().entrySet().stream()
            .filter(e -> !e.getValue().contains(AuthoritiesConstants.PROJECT_ADMIN))
            .map(e -> e.getKey())
            .collect(Collectors.toSet());

        notPermitted.stream()
            .forEach(p -> assertFalse(RadarAuthorization.hasPermissionInProject(token, p, project)));
    }

    @Test
    public void testIsSuperUser() {
        assertFalse(RadarAuthorization.isSuperUser(TokenTestUtils.PROJECT_ADMIN_TOKEN));
        assertTrue(RadarAuthorization.isSuperUser(TokenTestUtils.SUPER_USER_TOKEN));
    }

    @Test
    public void testHasPermission() {
        DecodedJWT token = TokenTestUtils.SUPER_USER_TOKEN;
        Permission.allPermissions().stream()
            .forEach(p -> assertTrue(RadarAuthorization.hasPermission(token, p)));
    }
}
