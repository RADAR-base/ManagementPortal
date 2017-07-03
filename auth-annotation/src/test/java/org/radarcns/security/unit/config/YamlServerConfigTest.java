package org.radarcns.security.unit.config;

import org.junit.Test;
import org.radarcns.security.config.ServerConfig;
import org.radarcns.security.config.YamlServerConfig;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by dverbeec on 19/06/2017.
 */
public class YamlServerConfigTest {

    @Test
    public void testLoadYamlFile() throws IOException, URISyntaxException {
        ServerConfig config = YamlServerConfig.readFromClasspath();
        assertEquals("test_username", config.getUsername());
        assertEquals("test_password", config.getPassword());
        assertEquals("http://localhost:8089/oauth/check_token", config.getTokenValidationEndpoint());
        assertEquals("http://localhost:8089/oauth/token_key", config.getPublicKeyEndpoint());
    }
}
