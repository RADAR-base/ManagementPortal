/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.web.rest.criteria

enum class SubjectSortBy(
    /** Query parameter name.  */
    val queryParam: String,
    /** Whether this property is unique across all subjects.  */
    val isUnique: Boolean,
) {
    ID("id", true),
    EXTERNAL_ID("externalId", false),
    USER_LOGIN("login", true),
}
