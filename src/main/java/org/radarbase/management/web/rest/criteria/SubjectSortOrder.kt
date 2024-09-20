/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.web.rest.criteria

import org.springframework.data.domain.Sort
import java.util.*
import javax.validation.constraints.NotNull

class SubjectSortOrder
    @JvmOverloads
    constructor(
        @NotNull val sortBy: SubjectSortBy,
        @NotNull var direction: Sort.Direction = Sort.Direction.ASC,
    ) {
        override fun toString(): String = sortBy.name + ',' + direction.name

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val that = other as SubjectSortOrder
            return sortBy == that.sortBy && direction == that.direction
        }

        override fun hashCode(): Int = Objects.hash(direction, sortBy)
    }
