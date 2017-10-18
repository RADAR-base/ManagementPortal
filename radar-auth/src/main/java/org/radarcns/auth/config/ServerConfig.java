package org.radarcns.auth.config;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;

public interface ServerConfig {

    /**
     * Get the public key endpoint as a URI.
     * @return The public key endpoint URI, or <code>null</code> if not defined
     */
    URI getPublicKeyEndpoint();

    /**
     * The name of this resource. It should be in the list of allowed resources for the OAuth
     * client.
     * @return the name of the resource
     */
    String getResourceName();

    /**
     * Get the public key set in the config file.
     * @return The public key, or <code>null</code> if not defined
     */
    RSAPublicKey getPublicKey();

}
