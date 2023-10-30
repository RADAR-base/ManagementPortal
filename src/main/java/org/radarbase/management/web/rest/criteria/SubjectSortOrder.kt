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

class SubjectSortOrder @JvmOverloads constructor(
    val sortBy: @NotNull SubjectSortBy,
    var direction: @NotNull Sort.Direction = Sort.Direction.ASC
) {

    override fun toString(): String {
        return sortBy.name + ',' + direction.name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as SubjectSortOrder
        return sortBy == that.sortBy && direction == that.direction
    }

    override fun hashCode(): Int {
        return Objects.hash(direction, sortBy)
    }
}
