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
    val attributes: Map<String, List<String>>? = null,
    val enabled: Boolean = true,
    val requiredActions: List<String>? = null,
    val realmRoles: List<String>? = null,
    val clientRoles: Map<String, List<String>>? = null,
    val groups: List<String>? = null,
)
