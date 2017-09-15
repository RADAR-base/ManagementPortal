package org.radarcns.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.security.annotation.Secured;
import org.radarcns.security.authorization.AuthorizationHandler;
import org.radarcns.security.authorization.RadarAuthorizationHandler;
import org.radarcns.security.config.YamlServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
/**
 * Token Authentication filter for RADAR-CNS. This class expects three system environment
 * variable to be present: IDENTITY_SERVER_CONFIG, which is a path to the yml file containing the
 * configuration for the OAuth2.0 server token verification. This authentication filter will check
 * the validity of the supplied 'Bearer' token, i.e., if it exists in the identity server AND if
 * it has the appropriate scope. If the token is deemed valid, this filter will go on to override
 * the request contexts security context, populating the principal with the username and user roles.
 */
public class TokenAuthenticationFilter implements ContainerRequestFilter {

    protected static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Context
    protected ResourceInfo resourceInfo;

    protected final AuthorizationHandler authorizationHandler;

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable.
     * @throws IOException The configuration file is not accessible
     * @throws InvalidKeySpecException
     */
    public TokenAuthenticationFilter() throws IOException, InvalidKeySpecException,
        NoSuchAlgorithmException, NotAuthorizedException {
        authorizationHandler = new RadarAuthorizationHandler(YamlServerConfig.readFromFileOrClasspath());
    }


    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = getToken(requestContext);
        try {
            // Validate the token
            DecodedJWT jwt = authorizationHandler.validateAccessToken(token);
            checkScopes(jwt);
            requestContext.setSecurityContext(
                createSecurityContext(jwt.getClaim("sub").asString(),
                                      jwt.getClaim("roles").asList(String.class)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    public String getToken(ContainerRequestContext requestContext) {
        // Check if the HTTP Authorization header is present and formatted correctly
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.error("No authorization header provided in the request");
            throw new NotAuthorizedException("Authorization header must be provided", Response
                .status(Response.Status.FORBIDDEN));
        }

        log.debug("Received bearer token from client");

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring("Bearer".length()).trim();
    }

    public void checkScopes(DecodedJWT token) {
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<String> methodScopes = extractScopes(resourceMethod);
        List<String> tokenScopes = token.getClaim("scope").asList(String.class);

        if (methodScopes.isEmpty()) {
            log.debug("Method is secured but no scopes defined, using scopes "
                        + "defined on class level to check authorization");
            // Get the resource class which matches with the requested URL
            // Extract the scopes declared by it
            Class<?> resourceClass = resourceInfo.getResourceClass();
            List<String> classScopes = extractScopes(resourceClass);
            checkTokenScope(classScopes, tokenScopes);
        } else {
            log.debug("Checking allowed method scopes against token scopes");
            checkTokenScope(methodScopes, tokenScopes);
        }

        log.debug("Token is authorized to access this resource");
    }

    public void checkTokenScope(List<String> scopesAllowed, List<String> tokenScopes)
                throws NotAuthorizedException {
        if (scopesAllowed.isEmpty()) {
            log.debug("No allowed scopes defined, assuming any valid token is authorized.");
            return;
        }
        for (String allowedScope : scopesAllowed) {
            for (String tokenScope : tokenScopes) {
                if (allowedScope.equalsIgnoreCase(tokenScope)) {
                    log.debug("Found matching scope in token scopes and allowed scopes: "
                                + tokenScope);
                    return;
                }
            }
        }
        throw new NotAuthorizedException("Token does not have the appropriate scope. Token is not "
            + "valid for this resource!", Response.status(Response.Status.FORBIDDEN));
    }

    public List<String> extractScopes(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<String>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<String>();
            } else {
                String[] scopesAllowed = secured.scopesAllowed();
                if (scopesAllowed != null) {
                    return Arrays.asList(scopesAllowed);
                }
                else {
                    return new ArrayList<>();
                }
            }
        }
    }

    public SecurityContext createSecurityContext(String name, List<String> roles) {
        return new SecurityContext() {

            public Principal getUserPrincipal() {
                return () -> name;
            }

            public boolean isUserInRole(String role) {
                return roles.contains(role);
            }

            public boolean isSecure() {
                return authorizationHandler.getIdentityServerConfig().getPublicKeyEndpoint()
                    .startsWith("https");
            }

            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        };
    }
}
