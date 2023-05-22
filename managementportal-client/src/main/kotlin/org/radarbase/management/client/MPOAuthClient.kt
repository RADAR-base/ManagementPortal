package org.radarbase.management.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MPOAuthClient(
    @SerialName("clientId") val id: String,
    var clientSecret: String? = null,
    val scope: List<String> = listOf(),
    val resourceIds: List<String> = listOf(),
    val authorizedGrantTypes: List<String> = listOf(),
    val autoApproveScopes: List<String> = listOf(),
    val accessTokenValiditySeconds: Long? = null,
    val refreshTokenValiditySeconds: Long? = null,
    val authorities: List<String> = listOf(),
    val registeredRedirectUri: List<String> = listOf(),
    val additionalInformation: Map<String, String> = mapOf(),
)
