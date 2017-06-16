package org.radarcns.security.filter;

import org.radarcns.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dverbeec on 17/03/2017.
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
/**
 * Authorization filter for RADAR. This class checks if the authenticated user is actually
 * authorized to perform the requested action. It is assumed the security context is already
 * populated with the correct user name and user roles.
 */
public class UserAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserAuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        SecurityContext securityContext = requestContext.getSecurityContext();

        MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo()
                    .getPathParameters();

        // Get the resource method which matches with the requested URL
        // Extract the scopes declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        Set<String> allowedRoles = extractRoles(resourceMethod);

        if (allowedRoles.isEmpty()) {
            log.debug("Method is secured but no roles defined, using roles "
                        + "defined on class level to check authorization");
            // Get the resource class which matches with the requested URL
            // Extract the scopes declared by it
            Class<?> resourceClass = resourceInfo.getResourceClass();
            allowedRoles = extractRoles(resourceClass);
        }

        try {
            isUserAuthorized(securityContext, allowedRoles, pathParameters);
        } catch (Exception e) {
            log.error(e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN).build());
        }
        log.debug("User is authorized for this resource");
    }

    // Extract the scopes from the annotated element
    private Set<String> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new HashSet<>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new HashSet<>();
            } else {
                String[] rolesAllowed = secured.rolesAllowed();
                return new HashSet<>(Arrays.asList(rolesAllowed));
            }
        }
    }


    private void checkUserRoles(Set<String> rolesAllowed, SecurityContext securityContext)
                throws NotAuthorizedException {
        log.debug("Checking user roles");
        if (rolesAllowed.isEmpty()) {
            log.debug("No allowed roles defined, assuming any role is authorized "
                        + "for this resource");
            return;
        }
        for (String role : rolesAllowed) {
            if (securityContext.isUserInRole(role)) {
                log.debug("User role " + role + " matched allowed role");
                return;
            }
        }
        throw new NotAuthorizedException("User does not have the appropriate role for this method");
    }

    private boolean isUserInStudy(String userId, String studyId) {
        //TODO this needs to be implemented
        return true;
    }

    private boolean areUsersInSameStudy(String userId1, String userId2) {
        //TODO
        return true;
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
    private void isUserAuthorized(SecurityContext securityContext, Set<String> rolesAllowed,
                MultivaluedMap<String, String> pathParameters) throws NotAuthorizedException {
        String authorizedUserId = securityContext.getUserPrincipal().getName();
        if (pathParameters.keySet().contains("userID")) {
            String requestedUserId = pathParameters.getFirst("userID");
            if (requestedUserId.equals(authorizedUserId)) {
                log.debug("User requested own data, this is authorized");
                return;
            } else {
                if (areUsersInSameStudy(authorizedUserId, requestedUserId)) {
                    log.debug("User " + authorizedUserId + " requested data for other user "
                                + requestedUserId + " in the same study");
                    checkUserRoles(rolesAllowed, securityContext);
                    return;
                } else {
                    throw new NotAuthorizedException("User " + authorizedUserId + " requested "
                                + "data for user " + requestedUserId + " but they are not in the "
                                + "same study. Not granting access.");
                }
            }
        } else {
            if (pathParameters.keySet().contains("studyID")) {
                String requestedStudyId = pathParameters.getFirst("studyID");
                if (isUserInStudy(authorizedUserId, requestedStudyId)) {
                    checkUserRoles(rolesAllowed, securityContext);
                    return;
                } else {
                    throw new NotAuthorizedException("User " + authorizedUserId + " requested "
                                + "access " + "to information regarding " + " study "
                                + requestedStudyId + ", but is not a member of this study. Not "
                                + "granting access.");
                }
            } else {
                checkUserRoles(rolesAllowed, securityContext);
                return;
            }
        }
    }
}
