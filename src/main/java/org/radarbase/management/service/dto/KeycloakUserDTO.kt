package org.radarbase.management.service.dto

import kotlinx.serialization.Serializable

@Serializable
data class KeycloakUserDTO(
    val id: String? = null,
    val username: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val attributes: Map<String, Set<String>>? = null,
    val enabled: Boolean = true,
    val requiredActions: List<String>? = null,
    val realmRoles: Set<String>? = null,
    val clientRoles: Map<String, Set<String>>? = null,
    val groups: Set<String>? = null,
)
