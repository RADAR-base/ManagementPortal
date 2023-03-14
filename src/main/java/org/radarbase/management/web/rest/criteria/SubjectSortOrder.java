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
import java.util.Objects;

public class SubjectSortOrder {
    private Sort.Direction direction;
    private final SubjectSortBy sortBy;

    public SubjectSortOrder(@NotNull SubjectSortBy sortBy,
            @NotNull Sort.Direction direction) {
        this.direction = direction;
        this.sortBy = sortBy;
    }

    public SubjectSortOrder(SubjectSortBy sortBy) {
        this(sortBy, Sort.Direction.ASC);
    }

    public void setDirection(@NotNull Sort.Direction direction) {
        this.direction = direction;
    }

    @NotNull
    public Sort.Direction getDirection() {
        return direction;
    }

    @NotNull
    public SubjectSortBy getSortBy() {
        return sortBy;
    }

    @Override
    public String toString() {
        return sortBy.name() + ',' + direction.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubjectSortOrder that = (SubjectSortOrder) o;

        return sortBy == that.sortBy && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, sortBy);
    }
}
