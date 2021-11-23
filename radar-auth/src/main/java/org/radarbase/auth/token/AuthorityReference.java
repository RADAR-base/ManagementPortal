/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.token;

import org.radarbase.auth.authorization.AuthoritiesConstants;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class AuthorityReference {
    private final String authority;
    private final String referent;
    private final AuthoritiesConstants role;

    public AuthorityReference(String authority) {
        this(authority, null);
    }

    public AuthorityReference(AuthoritiesConstants role) {
        this(role, null);
    }

    public AuthorityReference(AuthoritiesConstants role, String referent) {
        this.role = requireNonNull(role);
        this.authority = role.role();
        this.referent = referent;
    }

    public AuthorityReference(String authority, String referent) {
        this.authority = requireNonNull(authority);
        this.role = AuthoritiesConstants.valueOfRoleOrNull(authority);
        this.referent = referent;
    }

    public AuthoritiesConstants getRole() {
        return role;
    }

    public String getReferent() {
        return referent;
    }

    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthorityReference that = (AuthorityReference) o;

        return Objects.equals(referent, that.referent) && authority.equals(that.authority);
    }

    @Override
    public int hashCode() {
        int result = referent != null ? referent.hashCode() : 0;
        result = 31 * result + authority.hashCode();
        return result;
    }
}
