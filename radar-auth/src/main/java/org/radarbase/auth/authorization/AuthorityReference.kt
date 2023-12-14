/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.auth.authorization

import org.radarbase.auth.authorization.RoleAuthority.Companion.valueOfAuthority
import java.io.Serializable

/**
 * An authority referenced to a specific entity. Only roles with global scope do not need a
 * referent.
 */
data class AuthorityReference(
    val role: RoleAuthority,
    val authority: String,
    val referent: String?,
): Serializable {
    init {
        require(role.scope == RoleAuthority.Scope.GLOBAL || referent != null) { "Non-global authority references require a referent entity" }
    }

    /**
     * Authority reference with given role and the object it refers to.
     * @param role user role.
     * @param referent reference.
     */
    @JvmOverloads
    constructor(role: RoleAuthority, referent: String? = null) : this(role, role.authority, referent)

    /**
     * Authority reference with given authority and the object it refers to.
     * @param authority user authority.
     * @param referent reference.
     */
    @JvmOverloads
    constructor(authority: String, referent: String? = null) : this(valueOfAuthority(authority), authority, referent)
}
