package org.radarbase.auth.hydra

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class HydraOAuthClientDTO(
    @SerialName("client_id") val clientId: String,
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("redirect_uris") val redirectUris: List<String>? = null,
    @SerialName("grant_types") val grantTypes: List<String>? = null,
    @SerialName("response_types") val responseTypes: List<String>? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("audience") val audience: List<String>? = null,
    @SerialName("owner") val owner: String? = null,
    @SerialName("policy_uri") val policyUri: String? = null,
    @SerialName("allowed_cors_origins") val allowedCorsOrigins: List<String>? = null,
    @SerialName("tos_uri") val tosUri: String? = null,
    @SerialName("client_uri") val clientUri: String? = null,
    @SerialName("logo_uri") val logoUri: String? = null,
    @SerialName("contacts") val contacts: List<String>? = null,
    @SerialName("client_secret_expires_at") val clientSecretExpiresAt: Long? = null,
    @SerialName("subject_type") val subjectType: String? = null,
    @SerialName("jwks_uri") val jwksUri: String? = null,
    @SerialName("jwks") val jwks: JsonObject? = null,
    @SerialName("token_endpoint_auth_method") val tokenEndpointAuthMethod: String? = null,
    @SerialName("userinfo_signed_response_alg") val userinfoSignedResponseAlg: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Additional fields that may be present in Hydra responses
    @SerialName("post_logout_redirect_uris") val postLogoutRedirectUris: List<String>? = null,
    @SerialName("metadata") val metadata: JsonElement? = null,
    @SerialName("skip_consent") val skipConsent: Boolean? = null,
    @SerialName("skip_logout_consent") val skipLogoutConsent: Boolean? = null,
    @SerialName("authorization_code_grant_access_token_lifespan") val authorizationCodeGrantAccessTokenLifespan: String? = null,
    @SerialName("authorization_code_grant_id_token_lifespan") val authorizationCodeGrantIdTokenLifespan: String? = null,
    @SerialName("authorization_code_grant_refresh_token_lifespan") val authorizationCodeGrantRefreshTokenLifespan: String? = null,
    @SerialName("client_credentials_grant_access_token_lifespan") val clientCredentialsGrantAccessTokenLifespan: String? = null,
    @SerialName("implicit_grant_access_token_lifespan") val implicitGrantAccessTokenLifespan: String? = null,
    @SerialName("implicit_grant_id_token_lifespan") val implicitGrantIdTokenLifespan: String? = null,
    @SerialName("jwt_bearer_grant_access_token_lifespan") val jwtBearerGrantAccessTokenLifespan: String? = null,
    @SerialName("refresh_token_grant_id_token_lifespan") val refreshTokenGrantIdTokenLifespan: String? = null,
    @SerialName("refresh_token_grant_access_token_lifespan") val refreshTokenGrantAccessTokenLifespan: String? = null,
    @SerialName("refresh_token_grant_refresh_token_lifespan") val refreshTokenGrantRefreshTokenLifespan: String? = null
)
