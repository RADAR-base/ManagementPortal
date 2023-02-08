package org.radarbase.management.service

import org.radarbase.auth.authorization.*
import org.radarbase.auth.exception.NotAuthorizedException
import org.radarbase.auth.token.RadarToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer
import kotlin.jvm.Throws

@Service
open class AuthService {
    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired(required = false)
    private var token: RadarToken? = null

    private val oracle: AuthorizationOracle = AuthorizationOracle(
        object : EntityRelationService {
            override fun findOrganizationOfProject(project: String): String {
                return projectService.findOneByName(project).organization.name
            }
        }
    )

    @Throws(NotAuthorizedException::class)
    open fun checkScope(permission: Permission) {
        val token = token ?: throw NotAuthorizedException("User without authentication does not have permission.")
        oracle.checkScope(token, permission)
    }

    @JvmOverloads
    @Throws(NotAuthorizedException::class)
    open fun checkPermission(
        permission: Permission,
        builder: Consumer<EntityDetails>? = null,
        scope: Permission.Entity = permission.entity,
    ) {
        val token = token ?: throw NotAuthorizedException("User without authentication does not have permission.")
        oracle.checkPermission(token, permission, if (builder != null) entityDetailsBuilder(builder) else EntityDetails.global, scope)
    }

    @JvmOverloads
    open fun hasPermission(
        permission: Permission,
        builder: Consumer<EntityDetails>? = null,
        scope: Permission.Entity = permission.entity,
    ): Boolean {
        val token = token ?: return false
        return oracle.hasPermission(
            token,
            permission,
            if (builder != null) entityDetailsBuilder(builder) else EntityDetails.global,
            scope
        )
    }

    @JvmOverloads
    open fun hasPermission(
        permission: Permission,
        entityDetails: EntityDetails,
        scope: Permission.Entity = permission.entity,
    ): Boolean {
        val token = token ?: return false
        return oracle.hasPermission(token, permission, entityDetails, scope)
    }

    @JvmOverloads
    @Throws(NotAuthorizedException::class)
    open fun checkPermission(
        permission: Permission,
        entityDetails: EntityDetails,
        scope: Permission.Entity = permission.entity,
    ) {
        val token = token ?: throw NotAuthorizedException("User without authentication does not have permission.")
        oracle.checkPermission(token, permission, entityDetails, scope)
    }

    open fun referentsByScope(permission: Permission): AuthorityReferenceSet {
        val token = token ?: return AuthorityReferenceSet()
        return oracle.referentsByScope(token, permission)
    }
}
