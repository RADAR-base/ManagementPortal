/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.security;

import org.jetbrains.annotations.NotNull;
import org.radarbase.auth.token.AbstractRadarToken;
import org.radarbase.auth.token.AuthorityReference;
import org.radarbase.auth.token.RadarToken;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SessionRadarToken extends AbstractRadarToken implements Serializable {
    private final Set<AuthorityReference> roles;
    private final String subject;
    private final String token;
    private final Set<String> scopes;
    private final List<String> audience;
    private final List<String> sources;
    private final String grantType;
    private final Date issuedAt;
    private final Date expiresAt;
    private final String issuer;
    private final String clientId;
    private final String type;
    private final String username;

    /** Instantiate a serializable session token by copying an existing RadarToken. */
    public SessionRadarToken(RadarToken token) {
        this(token, token.getRoles());
    }

    /** Instantiate a serializable session token by copying an existing RadarToken. */
    private SessionRadarToken(RadarToken token, Set<AuthorityReference> roles) {
        this.roles = Set.copyOf(roles);
        this.subject = token.getSubject();
        this.token = token.getToken();
        this.scopes = Set.copyOf(token.getScopes());
        this.audience = List.copyOf(token.getAudience());
        this.sources = List.copyOf(token.getSources());
        this.grantType = token.getGrantType();
        this.issuedAt = token.getIssuedAt();
        this.expiresAt = token.getExpiresAt();
        this.issuer = token.getIssuer();
        this.clientId = token.getClientId();
        this.type = token.getType();
        this.username = token.getUsername();
    }

    @NotNull
    @Override
    public Set<AuthorityReference> getRoles() {
        return roles;
    }

    @NotNull
    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    @NotNull
    @Override
    public List<String> getSources() {
        return sources;
    }

    @NotNull
    @Override
    public String getGrantType() {
        return grantType;
    }

    @NotNull
    @Override
    public String getSubject() {
        return subject;
    }

    @NotNull
    @Override
    public Date getIssuedAt() {
        return issuedAt;
    }

    @NotNull
    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }

    @NotNull
    @Override
    public List<String> getAudience() {
        return audience;
    }

    @NotNull
    @Override
    public String getToken() {
        return token;
    }

    @NotNull
    @Override
    public String getIssuer() {
        return issuer;
    }

    @NotNull
    @Override
    public String getType() {
        return type;
    }

    @NotNull
    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClaimString(@NotNull String name) {
        return "";
    }

    @NotNull
    @Override
    public List<String> getClaimList(@NotNull String name) {
        return List.of();
    }

    @NotNull
    @Override
    public String getUsername() {
        return username;
    }

    public SessionRadarToken withRoles(Set<AuthorityReference> roles) {
        return new SessionRadarToken(this, roles);
    }

    /**
     * Create a new token.
     * @return null if provided null, a session radar token otherwise.
     */
    public static SessionRadarToken from(RadarToken token) {
        if (token == null) {
            return null;
        } else {
            return new SessionRadarToken(token);
        }
    }
}
