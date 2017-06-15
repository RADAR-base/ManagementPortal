package org.radarcns.security.config;

import org.radarcns.security.exceptions.NotConfiguredException;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class ManagementPortalConfig implements ServerConfig {
    // Base URL where the server lives
    private URI serverUrl;

    // OAuth client id and secret to access token validation endpoint
    private String clientId;
    private String clientSecret;

    private static final String TOKEN_VALIDATION_PATH = "/oauth/check_token";
    private static final String TOKEN_KEY_PATH = "/oauth/token_key";

    public ManagementPortalConfig() throws NotConfiguredException, URISyntaxException {
        String host = System.getenv("RADAR_IS_URL");
        if (host == null) {
            throw new NotConfiguredException("RADAR_IS_URL environment variable not set. "
                + "Set it to the root URL of your ManagementPortal instance.");
        }
        serverUrl = new URI(host);
        clientId = System.getenv("RADAR_IS_CLIENT_ID");
        clientSecret = System.getenv("RADAR_IS_CLIENT_SECRET");
        if (clientId == null) {
            throw new NotConfiguredException("RADAR_IS_CLIENT_ID environment variable not set. "
                + "Set it to this client's OAuth client ID received from ManagementPortal.");
        }
        if (clientSecret == null) {
            throw new NotConfiguredException("RADAR_IS_CLIENT_SECRET environment variable not set. "
                + "Set it to this client's OAuth client secret received from ManagementPortal.");
        }
    }


    @Override
    public URI tokenValidationEndpoint() {
        return UriBuilder.fromUri(serverUrl).path(TOKEN_VALIDATION_PATH).build();
    }

    @Override
    public URI publicKeyEndpoint() {
        return UriBuilder.fromUri(serverUrl).path(TOKEN_KEY_PATH).build();
    }

    @Override
    public String username() {
        return clientId;
    }

    @Override
    public String password() {
        return clientSecret;
    }
}
