/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static org.radarbase.management.web.rest.criteria.SubjectSortBy.ID;

@Component
public class SubjectSortByConverter implements Converter<String, SubjectSortBy> {
    /**
     * Parse sort direction from string.
     *
     * @param value user provided text
     * @return ASC for empty strings, ASC or DESC for corresponding strings, and UNKNOWN
     *         otherwise.
     */
    @Nullable
    @Override
    public SubjectSortBy convert(@Nullable String value) {
        if (StringUtils.isEmpty(value)) {
            return ID;
        }
        for (SubjectSortBy s : SubjectSortBy.values()) {
            if (s.getKey().equalsIgnoreCase(value)) {
                return s;
            }
        }
        return null;
    }
}
