package org.radarbase.management.service.dto;

import org.radarbase.auth.authorization.RoleAuthority;

public class AuthorityDTO {
    private String name;
    private String scope;

    public AuthorityDTO() {
        // POJO constructor
    }

    public AuthorityDTO(RoleAuthority role) {
        this.name = role.getAuthority();
        this.scope = role.getScope().name();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
