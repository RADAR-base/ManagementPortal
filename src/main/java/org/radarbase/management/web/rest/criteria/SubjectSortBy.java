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
    USER_LOGIN("login", true),
    USER_AUTHORITY("authority", false);

    private final String key;
    private final boolean isUnique;

    SubjectSortBy(String key, boolean isUnique) {
        this.key = key;
        this.isUnique = isUnique;
    }

    public String getKey() {
        return this.key;
    }

    public boolean isUnique() {
        return isUnique;
    }
}
