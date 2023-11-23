/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.repository.filters

import org.radarbase.management.domain.Subject
import org.radarbase.management.web.rest.criteria.CriteriaRange
import java.util.function.Supplier
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PredicateBuilder(val criteriaBuilder: CriteriaBuilder) {
    private val predicates: MutableList<Predicate>

    init {
        predicates = ArrayList()
    }

    /** Add predicate to query.  */
    fun add(predicate: Predicate?) {
        if (predicate == null) {
            return
        }
        predicates.add(predicate)
    }

    fun isNull(expression: Expression<*>?) {
        predicates.add(criteriaBuilder.isNull(expression))
    }

    /**
     * Build the predicates as an AND predicate.
     */
    fun toAndPredicate(): Predicate? {
        return if (predicates.size == 1) {
            predicates[0]
        } else if (predicates.isNotEmpty()) {
            criteriaBuilder.and(*predicates.toTypedArray<Predicate>())
        } else {
            null
        }
    }

    /**
     * Build the predicates as an AND predicate.
     */
    fun toOrPredicate(): Predicate? {
        return if (predicates.size == 1) {
            predicates[0]
        } else if (!predicates.isEmpty()) {
            criteriaBuilder.or(*predicates.toTypedArray<Predicate>())
        } else {
            null
        }
    }

    /**
     * Add an equal criteria to predicates if value is not null or empty.
     * @param path entity path
     * @param value value to compare with
     * @param <T> type of field.
    </T> */
    fun <T> equal(path: Supplier<Expression<T>?>, value: T) {
        if (isValidValue(value)) {
            add(criteriaBuilder.equal(path.get(), value))
        }
    }

    /**
     * Add an equal criteria to predicates if value is not null or empty.
     * @param path entity path
     * @param value value to compare with
     * @param <T> type of field.
    </T> */
    fun <T> equal(path: Expression<T>?, value: T) {
        if (isValidValue(value)) {
            add(criteriaBuilder.equal(path, value))
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param path entity path
     * @param value value to compare with
     */
    fun likeLower(path: Supplier<Expression<String?>?>, value: String) {
        if (isValidValue(value)) {
            add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(path.get()),
                    "%" + value.trim { it <= ' ' }.lowercase() + "%"
                )
            )
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param path entity path
     * @param value value to compare with
     */
    fun likeLower(path: Expression<String?>?, value: String?) {
        if (isValidValue(value)) {
            add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(path),
                    "%" + value!!.trim { it <= ' ' }.lowercase() + "%"
                )
            )
        }
    }

    /**
     * Add a like criteria to predicates if value is not null or empty, matching both sides.
     * @param root entity to fetch attributes from.
     * @param attributeKey name of the attribute.
     * @param attributeValue value to compare with using a like query.
     */
    fun attributeLike(
        root: Root<*>, attributeKey: String?,
        attributeValue: String?
    ) {
        if (isValidValue(attributeValue)) {
            val attributesJoin = root.joinMap<Subject, String, String>("attributes", JoinType.LEFT)
            add(
                criteriaBuilder.and(
                    criteriaBuilder.equal(attributesJoin.key(), attributeKey),
                    criteriaBuilder.like(
                        attributesJoin.value(),
                        "%$attributeValue%"
                    )
                )
            )
        }
    }

    fun `in`(expr: Expression<*>, other: Expression<*>?) {
        add(expr.`in`(other))
    }

    fun `in`(expr: Expression<*>, other: Collection<*>?) {
        add(expr.`in`(other))
    }

    /**
     * Add comparable criteria matching given range.
     * @param path entity property path.
     * @param range range that should be matched.
     */
    fun <T : Comparable<T>?> range(
        path: Path<out T>?, range: CriteriaRange<T>?
    ) {
        if (range == null || range.isEmpty) {
            return
        }
        range.validate()
        if (range.`is` != null) {
            add(criteriaBuilder.equal(path, range.`is`))
        } else {
            val from = range.from
            val to = range.to
            if (from != null && to != null) {
                add(criteriaBuilder.between(path, from, to))
            } else if (from != null) {
                add(criteriaBuilder.greaterThanOrEqualTo<T>(path, from))
            } else if (to != null) {
                add(criteriaBuilder.lessThanOrEqualTo<T>(path, to))
            }
        }
    }

    /**
     * Whether given String a proper value.
     */
    fun isValidValue(value: Any?): Boolean {
        if (value == null) {
            return false
        }
        return if (value is String) {
            !value.isBlank() && value != "null"
        } else true
    }

    fun newBuilder(): PredicateBuilder {
        return PredicateBuilder(criteriaBuilder)
    }

    val isEmpty: Boolean
        get() = predicates.isEmpty()
}
