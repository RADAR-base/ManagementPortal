package org.radarbase.management.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
data class ManagementPortalProperties @ConstructorBinding constructor(
    val identityServer: IdentityServer,
    val authServer: AuthServer,
    val mail: Mail,
    val frontend: Frontend,
    val oauth: Oauth,
    val common: Common,
    val catalogueServer: CatalogueServer,
    val account: Account ,
    val siteSettings: SiteSettings,
) {
    data class IdentityServer @ConstructorBinding constructor(
        val internal: Boolean = true,
        val serverUrl: String,
        val serverAdminUrl: String,
        val realm: String?,
        val adminEmail: String,
        val userActivationFlowType: String = "verification",
        val userActivationMethod: String = "code",
        // TODO replace by enum
        val type: String = "kratos"
    )

    data class AuthServer @ConstructorBinding constructor(
        val internal: Boolean = true,
        val serverUrl: String,
        val serverAdminUrl: String,
        val loginUrl: String,
        val keysPath: String = "/admin/keys/hydra.jwt.access-token",
    )

    data class Mail @ConstructorBinding constructor(
        val from: String,
    )

    data class Frontend @ConstructorBinding constructor(
        val clientId: String,
        val clientSecret: String,
        val accessTokenValiditySeconds: Int,
        val refreshTokenValiditySeconds: Int,
        val sessionTimeout: Int,
        val audience: String,
        val scopes: List<String> = listOf()
    )

    data class Oauth @ConstructorBinding constructor(
        val requireAal2: Boolean,
        val clientsFile: String?,
        val signingKeyAlias: String,
        val checkingKeyAliases: List<String>?,
        val keyStorePassword: String,
        val metaTokenTimeout: String,
        val persistentMetaTokenTimeout: String,
        val enablePublicKeyVerifiers: Boolean,
    )

    data class Common @ConstructorBinding constructor(
        val baseUrl: String,
        val managementPortalBaseUrl: String,
        val privacyPolicyUrl: String,
        val adminPassword: String,
        val activationKeyTimeoutInSeconds: Int,
    )

    data class CatalogueServer @ConstructorBinding constructor(
        val enableAutoImport: Boolean,
        val serverUrl: String,
    )

    data class Account @ConstructorBinding constructor(
        val enableExposeToken: Boolean,
    )

    data class SiteSettings @ConstructorBinding constructor(
        val hiddenSubjectFields: List<String>,
    )
}
