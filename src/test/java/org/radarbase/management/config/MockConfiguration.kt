/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.config

import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration
import java.time.Instant

@Configuration
class MockConfiguration {
    @Bean
    @Primary
    fun radarTokenMock(): RadarToken =
        DataRadarToken(
            subject = "admin",
            username = "admin",
            roles = setOf(AuthorityReference(RoleAuthority.SYS_ADMIN)),
            scopes = Permission.scopes().toSet(),
            grantType = "password",
            expiresAt = Instant.now() + Duration.ofMinutes(30),
        )
}
