package org.radarbase.management.service

import org.radarbase.auth.authorization.AuthorityReferenceSet
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import java.util.function.Consumer

interface AuthService {
    fun checkScope(permission: Permission);
    fun checkPermission(permission: Permission, builder: Consumer<EntityDetails>? = null, scope: Permission.Entity = permission.entity);
    fun referentsByScope(permission: Permission): AuthorityReferenceSet;
    fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean;
    fun mayBeGranted(authorities: Collection<RoleAuthority>, permission: Permission): Boolean;
    suspend fun fetchAccessToken(code: String): String;
}