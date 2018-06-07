package org.radarcns.auth.config;

import java.net.URI;
import java.security.PublicKey;
import java.util.List;

public interface ServerConfig {

    /**
     * Get the public key endpoint as a URI.
     * @return The public key endpoint URI, or <code>null</code> if not defined
     */
    List<URI> getPublicKeyEndpoints();

    /**
     * The name of this resource. It should be in the list of allowed resources for the OAuth
     * client.
     * @return the name of the resource
     */
    String getResourceName();

    /**
     * Get the public keys set in the config file.
     * @return The public keys, or <code>null</code> if not defined
     */
    List<PublicKey> getPublicKeys();

}
