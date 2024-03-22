/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.security;

import org.radarbase.auth.token.AbstractRadarToken;
import org.radarbase.auth.token.RadarToken;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionRadarToken extends AbstractRadarToken implements Serializable {
    private final Map<String, List<String>> roles;
    private final String subject;
    private final String token;
    private final List<String> scopes;
    private final List<String> audience;
    private final List<String> authorities;
    private final List<String> sources;
    private final String grantType;
    private final Date issuedAt;
    private final Date expiresAt;
    private final String issuer;
    private final String clientId;
    private final String type;
    private final String username;

    public SessionRadarToken(RadarToken token) {
        this.roles = token.getRoles().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, e -> e.getValue().stream()
                                        .map(String::toString)
                                        .collect(Collectors.toList())));
        this.subject = token.getSubject();
        this.token = token.getToken();
        this.scopes = List.copyOf(token.getScopes());
        this.audience = List.copyOf(token.getAudience());
        this.authorities = List.copyOf(token.getAuthorities());
        this.sources = List.copyOf(token.getSources());
        this.grantType = token.getGrantType();
        this.issuedAt = token.getIssuedAt();
        this.expiresAt = token.getExpiresAt();
        this.issuer = token.getIssuer();
        this.clientId = token.getClientId();
        this.type = token.getType();
        this.username = token.getUsername();
    }

    @Override
    public Map<String, List<String>> getRoles() {
        return roles;
    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public List<String> getSources() {
        return sources;
    }

    @Override
    public String getGrantType() {
        return grantType;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Date getIssuedAt() {
        return issuedAt;
    }

    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public List<String> getAudience() {
        return audience;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClaimString(String name) {
        return null;
    }

    @Override
    public List<String> getClaimList(String name) {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
