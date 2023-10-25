package org.radarbase.management.config;

import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.security.jwt.RadarTokenLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Configuration
public class RadarTokenConfiguration {
    private final RadarTokenLoader radarTokenLoader;

    @Autowired
    public RadarTokenConfiguration(RadarTokenLoader radarTokenLoader) {
        this.radarTokenLoader = radarTokenLoader;
    }

    @Scope(value = "request", proxyMode = TARGET_CLASS)
    @Bean
    @Nullable
    public RadarToken radarToken(HttpServletRequest request) {
        return radarTokenLoader.loadToken(request);
    }
}
