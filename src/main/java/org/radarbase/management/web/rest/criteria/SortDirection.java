/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String key;

    SortDirection(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
