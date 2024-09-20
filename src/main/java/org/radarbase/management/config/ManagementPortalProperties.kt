package org.radarbase.management.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
class ManagementPortalProperties {
    val identityServer: IdentityServer = IdentityServer()

    val mail: Mail = Mail()

    val frontend: Frontend = Frontend()

    var oauth: Oauth = Oauth()

    val common: Common = Common()

    val catalogueServer: CatalogueServer = CatalogueServer()

    val account: Account = Account()

    val siteSettings: SiteSettings = SiteSettings()

    class Account {
        var enableExposeToken: Boolean = false
    }

    class Common {
        var baseUrl: String = ""

        var managementPortalBaseUrl: String = ""

        var privacyPolicyUrl: String = ""

        var adminPassword: String = ""

        var activationKeyTimeoutInSeconds: Int = 24 * 60 * 60 // 1 day
    }

    class Mail {
        var from: String = ""
    }

    class Frontend {
        var clientId: String = ""

        var clientSecret: String = ""

        var accessTokenValiditySeconds: Int = 4 * 60 * 60

        var refreshTokenValiditySeconds: Int = 72 * 60 * 60

        var sessionTimeout: Int = 24 * 60 * 60 // a day
    }

    class Oauth {
        var requireAal2: Boolean = false

        var clientsFile: String? = null

        var signingKeyAlias: String? = null

        var checkingKeyAliases: List<String>? = null

        var keyStorePassword: String? = null

        var metaTokenTimeout: String? = null

        var persistentMetaTokenTimeout: String? = null

        var enablePublicKeyVerifiers: Boolean = false
    }

    class IdentityServer {
        var serverUrl: String? = null
        var serverAdminUrl: String? = null
        var adminEmail: String? = null
        var loginUrl: String? = null

        fun publicUrl(): String? = serverUrl

        fun adminUrl(): String? = serverAdminUrl
    }

    class CatalogueServer {
        var isEnableAutoImport: Boolean = false

        var serverUrl: String? = null
    }

    class SiteSettings {
        var hiddenSubjectFields: List<String>? = null
    }
}
