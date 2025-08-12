package org.radarbase.auth.kratos

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * DTO for Kratos JWT response that includes session data plus the tokenized JWT.
 * Extends the session structure with additional fields like verifiable_addresses,
 * recovery_addresses, and the actual JWT token.
 */
@Serializable
data class KratosJWTDTO(
    val id: String,
    val active: Boolean,
    @Serializable(with = InstantSerializer::class)
    val expires_at: Instant,
    @Serializable(with = InstantSerializer::class)
    val authenticated_at: Instant,
    val authenticator_assurance_level: String,
    val authentication_methods: List<AuthenticationMethod>,
    @Serializable(with = InstantSerializer::class)
    val issued_at: Instant,
    val identity: JWTIdentity,
    val devices: List<Device>,
    val tokenized: String
) {
    @Serializable
    data class AuthenticationMethod(
        val method: String? = null,
        val aal: String? = null,
        @Serializable(with = InstantSerializer::class)
        val completed_at: Instant? = null,
        val provider: String? = null
    )

    @Serializable
    data class Device(
        val id: String,
        val ip_address: String,
        val user_agent: String,
        val location: String
    )

    @Serializable
    data class JWTIdentity(
        val id: String? = null,
        val schema_id: String? = null,
        val schema_url: String? = null,
        val state: String? = null,
        @Serializable(with = InstantSerializer::class)
        val state_changed_at: Instant? = null,
        val traits: Traits? = null,
        val verifiable_addresses: List<VerifiableAddress>? = null,
        val recovery_addresses: List<RecoveryAddress>? = null,
        val metadata_public: Metadata? = null,
        @Serializable(with = InstantSerializer::class)
        val created_at: Instant? = null,
        @Serializable(with = InstantSerializer::class)
        val updated_at: Instant? = null,
        val organization_id: String? = null
    )

    @Serializable
    data class Traits(
        val name: String? = null,
        val email: String? = null,
        val projects: List<Project>? = null
    )

    @Serializable
    data class Project(
        val id: String? = null,
        val userId: String? = null,
        val name: String? = null
    )

    @Serializable
    data class VerifiableAddress(
        val id: String,
        val value: String,
        val verified: Boolean,
        val via: String,
        val status: String,
        @Serializable(with = InstantSerializer::class)
        val created_at: Instant,
        @Serializable(with = InstantSerializer::class)
        val updated_at: Instant
    )

    @Serializable
    data class RecoveryAddress(
        val id: String,
        val value: String,
        val via: String,
        @Serializable(with = InstantSerializer::class)
        val created_at: Instant,
        @Serializable(with = InstantSerializer::class)
        val updated_at: Instant
    )

    @Serializable
    data class Metadata(
        val roles: List<String> = emptyList(),
        val authorities: List<String> = emptyList(),
        val scope: List<String> = emptyList(),
        val sources: List<String> = emptyList(),
        val aud: List<String> = emptyList(),
        val mp_login: String? = null
    )
}
