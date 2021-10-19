/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

@Component
public class SubjectSortOrderConverter implements Converter<String, SubjectSortOrder> {
    /**
     * Parse sort direction from string.
     *
     * @param value user provided text
     * @return ASC for empty strings, ASC or DESC for corresponding strings, and UNKNOWN
     *         otherwise.
     */
    @Override
    @Nullable
    public SubjectSortOrder convert(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] subValues = value.split(":", 2);

        SubjectSortBy sortBy = Arrays.stream(SubjectSortBy.values())
                .filter(s -> s.getKey().equalsIgnoreCase(subValues[0]))
                .findAny()
                .orElseThrow(() -> new BadRequestException("Cannot convert sort property "
                        + subValues[0] + " to subject property", SUBJECT, ERR_VALIDATION));

        if (subValues.length == 2) {
            Sort.Direction direction = Sort.Direction.fromString(subValues[1]);
            return new SubjectSortOrder(sortBy, direction);
        } else {
            return new SubjectSortOrder(sortBy);
        }
    }
}
