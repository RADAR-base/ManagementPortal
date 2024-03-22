/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.security

import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.token.RadarToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal
import java.util.stream.Collectors
import javax.annotation.Nonnull

class RadarAuthentication(@param:Nonnull private val token: RadarToken) : Authentication, Principal {
    private val authorities: List<GrantedAuthority>
    private var isAuthenticated = true

    /** Instantiate authentication via a token.  */
    init {
        authorities = token.roles!!.stream()
            .map(AuthorityReference::authority)
            .distinct()
            .map { role: String? -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
    }

    override fun getName(): String {
        return if (token.isClientCredentials) {
            token.clientId!!
        } else {
            token.username!!
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getCredentials(): Any {
        return token
    }

    override fun getDetails(): Any? {
        return null
    }

    override fun getPrincipal(): Any? {
        return if (token.isClientCredentials) {
            null
        } else {
            this
        }
    }

    override fun isAuthenticated(): Boolean {
        return isAuthenticated
    }

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.isAuthenticated = isAuthenticated
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as RadarAuthentication
        return isAuthenticated == that.isAuthenticated && token == that.token
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + if (isAuthenticated) 1 else 0
        return result
    }

    override fun toString(): String {
        return ("RadarAuthentication{" + "token=" + token
                + ", authenticated=" + isAuthenticated()
                + '}')
    }
}
