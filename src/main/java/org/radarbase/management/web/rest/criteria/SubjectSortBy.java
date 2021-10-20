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
    ID("id", true),
    EXTERNAL_ID("externalId", false),
    USER_LOGIN("login", true);

    private final String queryParam;
    private final boolean isUnique;

    SubjectSortBy(String queryParam, boolean isUnique) {
        this.queryParam = queryParam;
        this.isUnique = isUnique;
    }

    /** Query parameter name. */
    public String getQueryParam() {
        return this.queryParam;
    }

    /** Whether this property is unique across all subjects. */
    public boolean isUnique() {
        return isUnique;
    }
}
