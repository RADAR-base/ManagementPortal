/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.config;

import org.radarbase.auth.token.RadarToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.radarbase.auth.authorization.AuthoritiesConstants.SYS_ADMIN;

@Configuration
public class MockConfiguration {
    @Bean
    @Primary
    public RadarToken radarTokenMock() {
        RadarToken token = mock(RadarToken.class);
        when(token.getSubject()).thenReturn("admin");
        when(token.hasAuthority(any())).thenAnswer(a -> a.getArgument(0).equals(SYS_ADMIN));
        when(token.hasPermission(any())).thenReturn(true);
        when(token.hasPermissionOnProject(any(), any())).thenReturn(true);
        when(token.hasPermissionOnSubject(any(), any(), any())).thenReturn(true);
        when(token.hasPermissionOnSource(any(), any(), any(), any())).thenReturn(true);
        when(token.isClientCredentials()).thenReturn(false);
        when(token.getAuthorities()).thenReturn(List.of(SYS_ADMIN));
        return token;
    }
}
