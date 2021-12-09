package org.radarbase.management.service.dto;

import org.radarbase.auth.authorization.RoleAuthority;

public class AuthorityDTO {
    private String scope;
    private String authority;

    public AuthorityDTO() {
    }

    public AuthorityDTO(RoleAuthority role) {
        this.scope = role.scope().name();
        this.authority = role.authority();
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
