package org.radarcns.security.authorization;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.exceptions.NotAuthorizedException;

import java.io.IOException;

/**
 * Basic Interface for Authorization related implementation.
 */
public interface AuthorizationHandler {

    /**
    * Check the validity of a token. If the token is valid, return the list of scopes the token
    * is valid for. Otherwise throw a NotAuthorizedException.
    *
    * @param token The token to check
    * @return The list of scopes if the token is valid
    * @throws NotAuthorizedException If the token is invalid
    * @throws IOException If there was an error communicating with the identity server
    */
    DecodedJWT validateAccessToken(String token) throws IOException, NotAuthorizedException;

    /**
     * Get the configuration for the identity server
     *
     * @return A ServerConfig instance.
     */
    ServerConfig getIdentityServerConfig();
}
