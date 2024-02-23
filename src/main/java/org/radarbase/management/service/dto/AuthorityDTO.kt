package org.radarbase.management.service.dto

import org.radarbase.auth.authorization.RoleAuthority

class AuthorityDTO {
    var name: String? = null
    var scope: String? = null

    constructor()
    constructor(role: RoleAuthority) {
        name = role.authority
        scope = role.scope.name
    }
}
