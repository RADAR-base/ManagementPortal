package org.radarcns.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.management.client.ApiException;
import org.radarcns.management.client.api.UserResourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Authorization helper class for RADAR. This class is used to communicate with Management Portal to
 * check if the authenticated user is allowed to access the protected resources of a given subject
 * based on the authorities and project affiliations.
 */
public class RadarAuthorization {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorization.class);
    private static final UserResourceApi RESOURCE_API = new UserResourceApi();
    private static final List<String> ROLES_READ_SUBJECT = Arrays.asList("ROLE_PROJECT_ADMIN",
        "ROLE_PROJECT_OWNER", "ROLE_PROJECT_AFFILIATE", "ROLE_PROJECT_ANALYST");
    private static final List<String> ROLES_ALTER_SUBJECT = Arrays.asList("ROLE_PROJECT_ADMIN",
        "ROLE_PROJECT_OWNER", "ROLE_PROJECT_AFFILIATE");

    public static boolean canUserAccessSubject(DecodedJWT token, String subjectLogin) {
        if (token.getClaim("authorities").asList(String.class).contains("ROLE_SYS_ADMIN")) {
            return true;
        }
        String projectName = getProjectNameForSubject(subjectLogin);
        return token.getClaim("roles").asList(String.class).stream()
            .filter(s -> s.contains(":"))       // make sure we have correctly formatted roles only
            .map(s -> s.split(":"))
            .filter(s -> s[0].equals(projectName))         // find roles for the project
            .filter(s -> ROLES_READ_SUBJECT.contains(s[1]))
            .findAny()
            .isPresent();
    }

    public static boolean canUserModifySubject(DecodedJWT token, String subjectLogin) {
        if (token.getClaim("authorities").asList(String.class).contains("ROLE_SYS_ADMIN")) {
            return true;
        }
        String projectName = getProjectNameForSubject(subjectLogin);
        return token.getClaim("roles").asList(String.class).stream()
            .filter(s -> s.contains(":"))       // make sure we have correctly formatted roles only
            .map(s -> s.split(":"))
            .filter(s -> s[0].equals(projectName))         // find roles for the project
            .filter(s -> ROLES_ALTER_SUBJECT.contains(s[1]))
            .findAny()
            .isPresent();
    }

    private static String getProjectNameForSubject(String subjectLogin) {
        try {
            Optional<String> projectName = RESOURCE_API.getUserUsingGET(subjectLogin).getRoles()
                .stream()
                .filter(r -> r.getAuthorityName().equals("ROLE_PARTICIPANT"))
                .map(r -> r.getProjectName())
                .findFirst();
            if (!projectName.isPresent()) {
                log.info("Access request for subject {}, but subject is not related to any project",
                    subjectLogin);
                throw new NotAuthorizedException("Subject {} is not related to any project", subjectLogin);
            }
            return projectName.get();
        } catch (ApiException e) {
            throw new NotAuthorizedException(e);
        }
    }
/*
    @Override
    public void filter(ContainerRequestContext requestContext) {

        SecurityContext securityContext = requestContext.getSecurityContext();

        // Get the resource method which matches with the requested URL
        // Extract the scopes declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<String> allowedRoles = extractRoles(resourceMethod);

        if (allowedRoles.isEmpty()) {
            log.debug("Method is secured but no roles defined, using roles "
                        + "defined on class level to check authorization");
            // Get the resource class which matches with the requested URL
            // Extract the scopes declared by it
            Class<?> resourceClass = resourceInfo.getResourceClass();
            allowedRoles = extractRoles(resourceClass);
        }

        try {
            isUserAuthorized(securityContext, allowedRoles);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
        log.debug("User is authorized for this resource");
    }

    // Extract the scopes from the annotated element
    public List<String> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<>();
            } else {
                String[] rolesAllowed = secured.rolesAllowed();
                if (rolesAllowed != null) {
                    return Arrays.asList(rolesAllowed);
                }
                else {
                    return new ArrayList<>();
                }
            }
        }
    }


    private void checkUserRoles(List<String> rolesAllowed, SecurityContext securityContext)
            throws NotAuthorizedException {
        log.debug("Checking user roles");
        if (rolesAllowed.isEmpty()) {
            log.debug("No allowed roles defined, assuming any role is authorized "
                        + "for this resource");
            return;
        }
        for (String role : rolesAllowed) {

            if (securityContext.isUserInRole(projectID + ":" + role)) {
                log.debug("User role " + role + " matched allowed role");
                return;
            }
        }
        throw new NotAuthorizedException("User does not have the appropriate role for this method",
            Response.status(Response.Status.FORBIDDEN));
    }

    /**
     * Checks if a user is authorized, based on the current security context, the allowed roles
     * for the requested operation and the path parameters. Check here for the flowchart:
     * https://docs.google.com/drawings/d/13G7INHdfTZSXLqKETzsudaaIHrLcU4V9Ab_kwkYZXTs/edit?ths=true
     * @param securityContext The current JAX-RS security context
     * @param rolesAllowed The roles that are allowed for this resource
     * @param pathParameters The path parameters supplied in the request. In practice, only in
     *                       certain conditions this is checked. See the flowchart for more details.
     * @throws NotAuthorizedException When the user is not authorized to access the resource
     */
    /*public void isUserAuthorized(SecurityContext securityContext, List<String> rolesAllowed,
                MultivaluedMap<String, String> pathParameters) throws NotAuthorizedException {
        String authorizedUserId = securityContext.getUserPrincipal().getName();
        String projectID = pathParameters.getFirst(PROJECT_PATH_PARAM);
        String userID = pathParameters.getFirst(USERID_PATH_PARAM);

        if (projectID == null) {
            throw new NotAuthorizedException(PROJECT_PATH_PARAM + " path parameter must be "
                + "supplied.", Response.status(Response.Status.FORBIDDEN));
        }

        if (userID != null && userID.equals(authorizedUserId)) {
            // user requested own data, this is allowed
            return;
        }

        checkUserRoles(rolesAllowed, projectID, securityContext);
    }*/
}
