/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;

public class SubjectSortOrder {
    @NotNull
    private final Sort.Direction direction;
    @NotNull
    private final SubjectSortBy sortBy;

    public SubjectSortOrder(SubjectSortBy sortBy, Sort.Direction direction) {
        this.direction = direction;
        this.sortBy = sortBy;
    }

    public SubjectSortOrder(SubjectSortBy sortBy) {
        this(sortBy, Sort.Direction.ASC);
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public SubjectSortBy getSortBy() {
        return sortBy;
    }

    @Override
    public String toString() {
        return sortBy.name() + ',' + direction.name();
    }
}
