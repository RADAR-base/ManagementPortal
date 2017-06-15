package org.radarcns.security.config;

import java.net.URI;

public interface ServerConfig {
    URI tokenValidationEndpoint();
    URI publicKeyEndpoint();
    String username();
    String password();
}
