/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.repository.filters;

import org.radarbase.management.domain.Subject;
import org.radarbase.management.web.rest.criteria.CriteriaRange;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class PredicateBuilder {
    private final List<Predicate> predicates;
    private final CriteriaBuilder builder;

    public PredicateBuilder(CriteriaBuilder builder) {
        this.predicates = new ArrayList<>();
        this.builder = builder;
    }

    /** Add predicate to query. */
    public void add(Predicate predicate) {
        if (predicate == null) {
            return;
        }
        predicates.add(predicate);
    }

    /**
     * Build the predicates as an AND predicate.
     */
    public Predicate toAndPredicate() {
        if (this.predicates.size() == 1) {
            return this.predicates.get(0);
        } else if (!this.predicates.isEmpty()) {
            return builder.and(this.predicates.toArray(new Predicate[0]));
        } else {
            return null;
        }
    }

    /**
     * Build the predicates as an AND predicate.
     */
    public Predicate toOrPredicate() {
        if (this.predicates.size() == 1) {
            return this.predicates.get(0);
        } else if (!this.predicates.isEmpty()) {
            return builder.or(this.predicates.toArray(new Predicate[0]));
        } else {
            return null;
        }
    }

    /**
     * Add an equal criteria to predicates if value is not null or empty.
     * @param path entity path
     * @param value value to compare with
     * @param <T> type of field.
     */
    public <T> void equal(Supplier<Expression<T>> path, T value) {
        if (isValidValue(value)) {
            add(builder.equal(path.get(), value));
        }
    }

    /**
     * Add an equal criteria to predicates if value is not null or empty.
     * @param path entity path
     * @param value value to compare with
     * @param <T> type of field.
     */
    public <T> void equal(Expression<T> path, T value) {
        if (isValidValue(value)) {
            add(builder.equal(path, value));
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param path entity path
     * @param value value to compare with
     */
    public void likeLower(Supplier<Expression<String>> path, String value) {
        if (isValidValue(value)) {
            add(builder.like(builder.lower(path.get()),
                    "%" + value.trim().toLowerCase(Locale.ROOT) + "%"));
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param path entity path
     * @param value value to compare with
     */
    public void likeLower(Expression<String> path, String value) {
        if (isValidValue(value)) {
            add(builder.like(builder.lower(path),
                    "%" + value.trim().toLowerCase(Locale.ROOT) + "%"));
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param root entity to fetch attributes from.
     * @param attributeKey name of the attribute.
     * @param attributeValue value to compare with using a like query.
     */
    public void attributeLike(Root<?> root, String attributeKey,
            String attributeValue) {
        if (isValidValue(attributeValue)) {
            MapJoin<Subject, String, String> attributesJoin =
                    root.joinMap("attributes", JoinType.LEFT);
            add(builder.and(
                    builder.equal(attributesJoin.key(), attributeKey),
                    builder.like(attributesJoin.value(),
                            "%" + attributeValue + "%")));
        }
    }

    public void in(Expression<?> expr, Expression<?> other) {
        add(expr.in(other));
    }

    public void in(Expression<?> expr, Collection<?> other) {
        add(expr.in(other));
    }

    /**
     * Add comparable criteria matching given range.
     * @param path entity property path.
     * @param range range that should be matched.
     */
    public <T extends Comparable<? super T>> void range(
            Path<? extends T> path, CriteriaRange<T> range) {
        if (range == null || range.isEmpty()) {
            return;
        }
        range.validate();
        if (range.getIs() != null) {
            add(builder.equal(path, range.getIs()));
        } else {
            if (range.getFrom() != null && range.getTo() != null) {
                add(builder.between(path, range.getFrom(), range.getTo()));
            } else if (range.getFrom() != null) {
                add(builder.greaterThanOrEqualTo(path, range.getFrom()));
            } else if (range.getTo() != null) {
                add(builder.lessThanOrEqualTo(path, range.getTo()));
            }
        }
    }

    /**
     * Whether given String a proper value.
     */
    public boolean isValidValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String str) {
            return !str.isBlank() && !str.equals("null");
        }
        return true;
    }

    public PredicateBuilder newBuilder() {
        return new PredicateBuilder(builder);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return builder;
    }

    public boolean isEmpty() {
        return this.predicates.isEmpty();
    }
}
