/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest.criteria;

public class CriteriaRange<T extends Comparable<? super T>> {
    private T from = null;
    private T to = null;
    private T is = null;

    public T getFrom() {
        return getIs() == null ? from : null;
    }

    public void setFrom(T from) {
        this.from = from;
    }

    public T getTo() {
        return getIs() == null ? to : null;
    }

    public void setTo(T to) {
        this.to = to;
    }

    public void setIs(T is) {
        this.is = is;
    }

    /** Whether the criteria is equal to this value. */
    public T getIs() {
        if (is != null) {
            return is;
        } else if (from != null && from.equals(to)) {
            return from;
        } else {
            return null;
        }
    }

    /**
     * Whether the criteria range contains any values at all.
     */
    public boolean isEmpty() {
        return from == null && to == null && is == null;
    }

    /**
     * Validate this criteria range whether the from and to ranges are in order.
     */
    public void validate() {
        if (is == null && from != null && to != null && from.compareTo(to) > 0) {
            throw new IllegalArgumentException(
                    "CriteriaRange must have a from range that is smaller then the to range.");
        }
    }

    @Override
    public String toString() {
        return "CriteriaRange{" + "from=" + getFrom()
                + ", to=" + getTo()
                + ", is=" + getIs()
                + '}';
    }
}
