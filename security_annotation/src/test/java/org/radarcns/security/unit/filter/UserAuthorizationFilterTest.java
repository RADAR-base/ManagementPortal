package org.radarcns.security.unit.filter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.security.annotation.Secured;
import org.radarcns.security.filter.UserAuthorizationFilter;
import org.radarcns.security.unit.util.TokenTestUtils;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.AnnotatedElement;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dverbeec on 3/07/2017.
 */
public class UserAuthorizationFilterTest {

    private static SecurityContext securityContext;
    private static UserAuthorizationFilter filter;

    @BeforeClass
    public static void setUp() {
        securityContext = createSecurityContext();
        filter = new UserAuthorizationFilter();
    }

    @Test
    public void testExtractRoles() {
        String[] roles = {"role1", "role2"};
        Secured secured = mock(Secured.class);
        when(secured.rolesAllowed()).thenReturn(roles);
        AnnotatedElement element = mock(AnnotatedElement.class);
        when(element.getAnnotation(Secured.class)).thenReturn(secured);
        List<String> extracted = filter.extractRoles(element);
        List<String> expected = Arrays.asList(roles);

        // test lists contain the same elements, disregarding order of items
        assertEquals(expected.size(), extracted.size());
        for (String scope : expected) {
            assertTrue(extracted.contains(scope));
        }
    }

    @Test
    public void testRolesEmptyAllowed() {
        // empty list of allowed roles
        List<String> allowed = new ArrayList<>();

        // let's get the projects defined in our TokenTestUtils
        List<String> projects = Arrays.asList(TokenTestUtils.ROLES).stream()
            .filter(role -> !role.startsWith(":"))
            .map(role -> role.substring(0, role.indexOf(":")))
            .collect(Collectors.toList());

        // check we are actually testing things
        assertTrue(projects.size() > 0);

        for (String project : projects) {
            MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
            pathParams.putSingle(UserAuthorizationFilter.PROJECT_PATH_PARAM, project);
            filter.isUserAuthorized(securityContext, allowed, pathParams);
        }
    }

    @Test
    public void testUserInProjectAllowed() {
        // let's get the projects defined in our TokenTestUtils
        List<String> roles = Arrays.asList(TokenTestUtils.ROLES).stream()
            .filter(role -> !role.startsWith(":"))
            .collect(Collectors.toList());

        // check we are actually testing things
        assertTrue(roles.size() > 0);

        for (String role : roles) {
            String project = role.substring(0, role.indexOf(":"));
            String auth = role.substring(role.indexOf(":") + 1);

            // just to make sure we have more then one roles allowed
            List<String> allowed = Arrays.asList(auth + "_1", auth);

            MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
            pathParams.putSingle(UserAuthorizationFilter.PROJECT_PATH_PARAM, project);
            filter.isUserAuthorized(securityContext, allowed, pathParams);
        }
    }

    @Test
    public void testUserOwnDataAllowed() {
        // let's get the projects defined in our TokenTestUtils
        List<String> roles = Arrays.asList(TokenTestUtils.ROLES).stream()
            .filter(role -> !role.startsWith(":"))
            .collect(Collectors.toList());

        // check we are actually testing things
        assertTrue(roles.size() > 0);

        for (String role : roles) {
            String project = role.substring(0, role.indexOf(":"));
            String auth = role.substring(role.indexOf(":") + 1);

            // just to make sure we have more then one roles allowed, but not the role we have
            List<String> allowed = Arrays.asList(auth + "_1", auth + "_2");

            MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
            pathParams.putSingle(UserAuthorizationFilter.PROJECT_PATH_PARAM, project);
            pathParams.putSingle(UserAuthorizationFilter.USERID_PATH_PARAM, TokenTestUtils.USER);
            filter.isUserAuthorized(securityContext, allowed, pathParams);
        }
    }

    @Test
    public void testUserInProjectNotAllowed() {
        // let's get the projects defined in our TokenTestUtils
        List<String> roles = Arrays.asList(TokenTestUtils.ROLES).stream()
            .filter(role -> !role.startsWith(":"))
            .collect(Collectors.toList());

        // check we are actually testing things
        assertTrue(roles.size() > 0);

        for (String role : roles) {
            String project = role.substring(0, role.indexOf(":"));
            String auth = role.substring(role.indexOf(":") + 1);

            // just to make sure we have more then one roles allowed, but not the role we have
            List<String> allowed = Arrays.asList(auth + "_1", auth + "_2");

            MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
            pathParams.putSingle(UserAuthorizationFilter.PROJECT_PATH_PARAM, project);
            try {
                filter.isUserAuthorized(securityContext, allowed, pathParams);
                fail("User should not be authorized to view this! Project is " + project + ", "
                    + "roles are [" + String.join(", ", allowed) + "]");
            }
            catch(NotAuthorizedException e) {
                // this is what should happen
            }
        }
    }

    @Test
    public void testUserNotInProjectNotAllowed() {
        // let's get the projects defined in our TokenTestUtils
        List<String> roles = Arrays.asList(TokenTestUtils.ROLES).stream()
            .filter(role -> !role.startsWith(":"))
            .collect(Collectors.toList());

        // check we are actually testing things
        assertTrue(roles.size() > 0);

        for (String role : roles) {
            String project = role.substring(0, role.indexOf(":")) + "_1";
            String auth = role.substring(role.indexOf(":") + 1);

            // just to make sure we have more then one roles allowed, but not the role we have
            List<String> allowed = Arrays.asList(auth + "_1", auth + "_2");

            MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
            pathParams.putSingle(UserAuthorizationFilter.PROJECT_PATH_PARAM, project);
            try {
                filter.isUserAuthorized(securityContext, allowed, pathParams);
                fail("User should not be authorized to view this! Project is " + project + ", "
                    + "roles are [" + String.join(", ", allowed) + "]");
            }
            catch(NotAuthorizedException e) {
                // this is what should happen
            }
        }
    }

    @Test(expected = NotAuthorizedException.class)
    public void testNoProjectIdSupplied() {
        filter.isUserAuthorized(securityContext, new ArrayList<>(), new MultivaluedHashMap<>());
    }

    private static SecurityContext createSecurityContext() {
        final String name = TokenTestUtils.USER;
        final List<String> roles = Arrays.asList(TokenTestUtils.ROLES);
        return new SecurityContext() {

            public Principal getUserPrincipal() {
                return () -> name;
            }

            public boolean isUserInRole(String role) {
                return roles.contains(role);
            }

            public boolean isSecure() {
                return false;
            }

            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        };
    }

}
