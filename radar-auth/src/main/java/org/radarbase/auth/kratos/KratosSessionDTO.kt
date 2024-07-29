package org.radarbase.auth.kratos

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import java.time.Instant


@Serializable
class KratosSessionDTO(
    val id: String,
    val active: Boolean,
    @Serializable(with = InstantSerializer::class)
    val expires_at: Instant,
    @Serializable(with = InstantSerializer::class)
    val authenticated_at: Instant,
    val authenticator_assurance_level: RadarToken.AuthenticatorAssuranceLevel,
    val authentication_methods: ArrayList<AuthenticationMethod>,
    @Serializable(with = InstantSerializer::class)
    val issued_at: Instant,
    val identity: Identity,
    val devices: ArrayList<Device>
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
    class Device(
        val id: String,
        val ip_address: String,
        val user_agent: String,
        val location: String,
    )

    @Serializable
    class Identity(
        val id: String? = null,
        val schema_id: String? = null,
        val schema_url: String? = null,
        val state: String? = null,
        @Serializable(with = InstantSerializer::class)
        val state_changed_at: Instant? = null,
        val traits: Traits? = null,
        val metadata_public: Metadata? = null,
        @Serializable(with = InstantSerializer::class)
        val created_at: Instant? = null,
        @Serializable(with = InstantSerializer::class)
        val updated_at: Instant? = null,
    )
    {


        fun parseRoles(): Set<AuthorityReference> = buildSet {
            if (metadata_public?.authorities?.isNotEmpty() == true) {
                for (roleValue in metadata_public.authorities) {
                    val authority = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (authority?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(authority))
                    }
                }
            }
            if (metadata_public?.roles?.isNotEmpty() == true) {
                for (roleValue in metadata_public.roles) {
                    val role = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (role?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(role))
                    }
                }
            }
        }
    }


    @Serializable
    class Traits (
        val name: String? = null,
        val email: String? = null,
    )

    @Serializable
    class Metadata (
        val roles: List<String>,
        val authorities: Set<String>,
        val scope: List<String>,
        val sources: List<String>,
        val aud: List<String>,
        val mp_login: String?
    )

    fun toDataRadarToken() : DataRadarToken {
        return DataRadarToken(
            roles = this.identity.parseRoles(),
            scopes = this.identity.metadata_public?.scope?.toSet() ?: emptySet(),
            sources = this.identity.metadata_public?.sources ?: emptyList(),
            grantType = "session",
            subject = this.identity.id,
            issuedAt = this.issued_at,
            expiresAt = this.expires_at,
            audience = this.identity.metadata_public?.aud ?: emptyList(),
            token = this.id,
            issuer = this.authentication_methods.first().provider,
            type = "type",
            clientId = "ManagementPortalapp",
            username = this.identity.metadata_public?.mp_login,
            authenticatorAssuranceLevel = this.authenticator_assurance_level
        )
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}
