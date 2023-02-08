/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.security;

import org.radarbase.auth.token.AuthorityReference;
import org.radarbase.auth.token.RadarToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RadarAuthentication implements Authentication, Principal {
    private final RadarToken token;
    private final List<GrantedAuthority> authorities;
    private boolean isAuthenticated;

    /** Instantiate authentication via a token. */
    public RadarAuthentication(@Nonnull RadarToken token) {
        this.token = token;
        isAuthenticated = true;
        authorities = token.getRoles().stream()
                .map(AuthorityReference::getAuthority)
                .distinct()
                .map(a -> (GrantedAuthority) () -> a)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        if (token.isClientCredentials()) {
            return token.getClientId();
        } else {
            return token.getUsername();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        if (token.isClientCredentials()) {
            return null;
        } else {
            return this;
        }
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RadarAuthentication that = (RadarAuthentication) o;

        return isAuthenticated == that.isAuthenticated
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (isAuthenticated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RadarAuthentication{" + "token=" + token
                + ", authenticated=" + isAuthenticated()
                + '}';
    }
}
