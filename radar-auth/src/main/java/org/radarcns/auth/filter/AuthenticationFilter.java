package org.radarcns.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.annotation.Secured;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
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
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
public class AuthenticationFilter implements ContainerRequestFilter {

    protected static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Context
    protected ResourceInfo resourceInfo;

    private final ServerConfig config;
    private JWTVerifier verifier;

    /**
     * Default constructor. Will load the identity server configuration from a file called
     * radar-is.yml that should be on the classpath, or its location defined in the
     * RADAR_IS_CONFIG_LOCATION environment variable.
     * @throws IOException The configuration file is not accessible
     * @throws InvalidKeySpecException
     */
    public AuthenticationFilter() throws IOException, InvalidKeySpecException,
        NoSuchAlgorithmException, NotAuthorizedException {
        this.config = YamlServerConfig.readFromFileOrClasspath();
        loadPublicKey();
    }


    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = getToken(requestContext);
        try {
            // Validate the token
            DecodedJWT jwt = validateAccessToken(token);
            checkScopes(jwt);
            requestContext.setSecurityContext(
                createSecurityContext(jwt.getClaim("sub").asString(),
                                      jwt.getClaim("roles").asList(String.class)));
            requestContext.setProperty("jwt", jwt);
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
            return Collections.emptyList();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return Collections.emptyList();
            } else {
                String[] scopesAllowed = secured.scopesAllowed();
                if (scopesAllowed != null) {
                    return Arrays.asList(scopesAllowed);
                }
                else {
                    return Collections.emptyList();
                }
            }
        }
    }

    public SecurityContext createSecurityContext(String name, List<String> roles) {

        // the roles here are of the form projectId:authority, we only need the authority, as we can
        // not infer the project in the authorization filter
        final Set<String> contextRoles = roles.stream()
                .map(r -> r.split(":")[1]).collect(Collectors.toSet());
        return new SecurityContext() {

            public Principal getUserPrincipal() {
                return () -> name;
            }

            public boolean isUserInRole(String role) {
                return contextRoles.contains(role);
            }

            public boolean isSecure() {
                return config.getPublicKeyEndpoint().getScheme().startsWith("https");
            }

            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        };
    }

    public DecodedJWT validateAccessToken(String token) throws TokenValidationException {
        try {
            return verifier.verify(token);
        }
        catch (JWTVerificationException ex) {
            // perhaps the server's key changed, let's fetch it again and re-check
            loadPublicKey();
            try {
                return verifier.verify(token);
            }
            catch(JWTVerificationException ex2) {
                throw new TokenValidationException(ex2);
            }
        }
    }

    private void loadPublicKey() throws TokenValidationException {
        RSAPublicKey publicKey = publicKeyFromServer();
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        verifier = JWT.require(alg)
            .withAudience(config.getResourceName())
            .build();
    }

    private RSAPublicKey publicKeyFromServer() throws TokenValidationException {
        log.debug("Getting the public key at " + config.getPublicKeyEndpoint());

        try {
            URLConnection connection =  config.getPublicKeyEndpoint().toURL().openConnection();
            connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode publicKeyInfo = mapper.readTree(inputStream);

                // we expect RSA algorithm, and deny to trust the public key otherwise
                // see also https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                if (!publicKeyInfo.get("alg").asText().equals("SHA256withRSA")) {
                    throw new NotAuthorizedException("The identity server reported the following "
                        + "signing algorithm: " + publicKeyInfo.get("alg") + ". Expected SHA256withRSA.");
                }

                String keyString = publicKeyInfo.get("value").asText();
                return publicKeyFromString(keyString);
            }
        }
        catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }

    private RSAPublicKey publicKeyFromString(String keyString) throws TokenValidationException {
        log.debug("Parsing public key: " + keyString);
        try(PemReader pemReader = new PemReader(new StringReader(keyString))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        }
        catch (Exception ex) {
            throw new TokenValidationException(ex);
        }
    }
}
