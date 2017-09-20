package org.radarcns.auth.unit.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by dverbeec on 19/06/2017.
 */
public class YamlServerConfigTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testLoadYamlFileFromClasspath() throws IOException {
        ServerConfig config = YamlServerConfig.readFromFileOrClasspath();
        assertEquals("http://localhost:8089/oauth/token_key", config.getMpBaseURI().toString());
        assertEquals("unit_test", config.getResourceName());
    }

    @Test
    public void testLoadYamlFileFromEnv() throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        File configFile = new File(loader.getResource(YamlServerConfig.CONFIG_FILE_NAME).getFile());
        environmentVariables.set(YamlServerConfig.LOCATION_ENV, configFile.getAbsolutePath());
        ServerConfig config = YamlServerConfig.readFromFileOrClasspath();
        assertEquals("http://localhost:8089/oauth/token_key", config.getMpBaseURI().toString());
        assertEquals("unit_test", config.getResourceName());
    }
}
