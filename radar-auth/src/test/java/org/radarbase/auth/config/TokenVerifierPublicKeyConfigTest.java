package org.radarbase.auth.config;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by dverbeec on 19/06/2017.
 */
class TokenVerifierPublicKeyConfigTest {

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    void testLoadYamlFileFromClasspath() throws URISyntaxException {
        TokenValidatorConfig config = TokenVerifierPublicKeyConfig.readFromFileOrClasspath();
        checkConfig(config);
    }

    @Test
    void testLoadYamlFileFromEnv() throws URISyntaxException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File configFile = new File(loader.getResource("radar-is.yml").toURI());
        environmentVariables
                .set(TokenVerifierPublicKeyConfig.LOCATION_ENV, configFile.getAbsolutePath());
        TokenValidatorConfig config = TokenVerifierPublicKeyConfig.readFromFileOrClasspath();
        checkConfig(config);
    }

    private void checkConfig(TokenValidatorConfig config) throws URISyntaxException {
        List<URI> uris = config.getPublicKeyEndpoints();
        assertThat(uris, hasItems(new URI("http://localhost:8089/oauth/token_key"),
                new URI("http://localhost:8089/oauth/token_key")));
        assertEquals(2, uris.size());
        assertEquals("unit_test", config.getResourceName());
    }
}
