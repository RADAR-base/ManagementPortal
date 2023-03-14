/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

public class SubjectCriteriaLast {
    private String id = null;
    private String externalId = null;
    private String login = null;
    private SubjectAuthority authority = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public SubjectAuthority getAuthority() {
        return authority;
    }

    public void setAuthority(SubjectAuthority authority) {
        this.authority = authority;
    }
}
