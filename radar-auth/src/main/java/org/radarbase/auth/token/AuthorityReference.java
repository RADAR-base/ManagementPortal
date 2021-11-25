/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.token;

import org.radarbase.auth.authorization.RoleAuthority;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class AuthorityReference implements Serializable {
    private final String authority;
    private final String referent;
    private final RoleAuthority role;

    public AuthorityReference(String authority) {
        this(authority, null);
    }

    public AuthorityReference(RoleAuthority role) {
        this(role, null);
    }

    /**
     * Authority reference with given role and the object it refers to.
     * @param role user role.
     * @param referent reference.
     */
    public AuthorityReference(RoleAuthority role, String referent) {
        this.role = requireNonNull(role);
        this.authority = role.authority();
        this.referent = referent;
    }

    /**
     * Authority reference with given authority and the object it refers to.
     * @param authority user authority.
     * @param referent reference.
     */
    public AuthorityReference(String authority, String referent) {
        this.authority = requireNonNull(authority);
        this.role = RoleAuthority.valueOfAuthorityOrNull(authority);
        this.referent = referent;
    }

    public RoleAuthority getRole() {
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
