package org.radarcns.auth.config;

import java.net.URI;

public interface ServerConfig {

    /**
     *
     * @return The Management Portal base URI
     */
    URI getMpBaseURI();

    /**
     * The name of this resource. It should be in the list of allowed resources for the OAuth client
     *
     * @return the name of the resource
     */
    String getResourceName();


}
