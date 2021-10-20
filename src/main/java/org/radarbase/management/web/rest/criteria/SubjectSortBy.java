/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

public enum SubjectSortBy {
    ID("id", "id", true),
    EXTERNAL_ID("externalId", "externalId", false),
    USER_LOGIN("login", "user.login", true),
    USER_AUTHORITY("authority", "user.authority.name", false);

    private final String queryParam;
    private final String dbField;
    private final boolean isUnique;

    SubjectSortBy(String queryParam, String dbField, boolean isUnique) {
        this.queryParam = queryParam;
        this.dbField = dbField;
        this.isUnique = isUnique;
    }

    /** Query parameter name. */
    public String getQueryParam() {
        return this.queryParam;
    }

    /** Database field relative to the Subject entity. */
    public String getDbField() {
        return dbField;
    }

    /** Whether this property is unique across all subjects. */
    public boolean isUnique() {
        return isUnique;
    }
}
