package org.radarcns.auth.config;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.radarcns.auth.util.TokenTestUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by dverbeec on 19/06/2017.
 */
public class YamlServerConfigTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testLoadYamlFileFromClasspath() throws IOException {
        ServerConfig config = YamlServerConfig.readFromFileOrClasspath();
        assertEquals("http://localhost:8089/oauth/token_key", config.getPublicKeyEndpoint().toString());
        assertEquals("unit_test", config.getResourceName());
    }

    @Test
    public void testLoadYamlFileFromEnv() throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        File configFile = new File(loader.getResource(YamlServerConfig.CONFIG_FILE_NAME).getFile());
        environmentVariables.set(YamlServerConfig.LOCATION_ENV, configFile.getAbsolutePath());
        ServerConfig config = YamlServerConfig.readFromFileOrClasspath();
        assertEquals("http://localhost:8089/oauth/token_key", config.getPublicKeyEndpoint().toString());
        assertNull(config.getPublicKey());
        assertEquals("unit_test", config.getResourceName());
    }

    @Test
    public void testLoadYamlFileWithPublicKey() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        File configFile = new File(loader.getResource("radar-is-2.yml").getFile());
        environmentVariables.set(YamlServerConfig.LOCATION_ENV, configFile.getAbsolutePath());
        ServerConfig config = YamlServerConfig.readFromFileOrClasspath();
        TokenTestUtils.setUp();
        assertEquals(TokenTestUtils.PUBLIC_KEY_STRING, new String(new Base64().encode(config
                .getPublicKey().getEncoded())));
        assertNull(config.getPublicKeyEndpoint());
        assertEquals("unit_test", config.getResourceName());
    }
}
