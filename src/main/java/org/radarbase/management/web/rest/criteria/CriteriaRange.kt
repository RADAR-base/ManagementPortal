/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.web.rest.criteria

open class CriteriaRange<T : Comparable<T>?>(var from: T? = null, var to: T? = null,
                                             var iss: T? = null
) {
    fun from(): T? {
        return if (this.iss == null) from else null
    }

    fun to(): T? {
        return if (this.iss == null) to else null
    }

    val isEmpty: Boolean
        /**
         * Whether the criteria range contains any values at all.
         */
        get() = from == null && to == null && iss == null

    /**
     * Validate this criteria range whether the from and to ranges are in order.
     */
    fun validate() {
        require(!(iss == null && from != null && to != null && from!!.compareTo(to!!) > 0)) { "CriteriaRange must have a from range that is smaller then the to range." }
    }

    override fun toString(): String {
        return ("CriteriaRange{" + "from=" + from()
                + ", to=" + to()
                + ", is=" + this.iss
                + '}')
    }
}
