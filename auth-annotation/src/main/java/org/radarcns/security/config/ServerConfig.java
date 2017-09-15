package org.radarcns.security.config;

public interface ServerConfig {

    /**
     *
     * @return The URL of the identity server's public key endpoint.
     */
    String getPublicKeyEndpoint();

    /**
     *
     * @return The username used to access the identity server.
     */
    String getUsername();

    /**
     *
     * @return The password used to access the identity server.
     */
    String getPassword();
}
