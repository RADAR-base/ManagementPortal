/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.config;

import org.radarbase.auth.authorization.Permission;
import org.radarbase.auth.token.AuthorityReference;
import org.radarbase.auth.token.RadarToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;

@Configuration
public class MockConfiguration {
    @Bean
    @Primary
    public RadarToken radarTokenMock() {
        RadarToken token = mock(RadarToken.class);
        when(token.getSubject()).thenReturn("admin");
        when(token.getUsername()).thenReturn("admin");
        when(token.isClientCredentials()).thenReturn(false);
        when(token.getRoles()).thenReturn(Set.of(new AuthorityReference(SYS_ADMIN)));
        when(token.getScopes()).thenReturn(new LinkedHashSet<>(Arrays.asList(Permission.scopes())));
        return token;
    }
}
