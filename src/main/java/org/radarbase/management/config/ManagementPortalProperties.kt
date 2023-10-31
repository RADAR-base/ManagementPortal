package org.radarbase.management.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by nivethika on 3-10-17.
 */
@ConfigurationProperties(prefix = "managementportal", ignoreUnknownFields = false)
class ManagementPortalProperties {
    val mail = Mail()
    @JvmField
    val frontend = Frontend()
    @JvmField
    var oauth = Oauth()
    @JvmField
    val common = Common()
    val catalogueServer = CatalogueServer()
    val account = Account()
    val siteSettings = SiteSettings()

    class Account {
        var enableExposeToken = false
    }

    class Common {
        var baseUrl = ""
        var managementPortalBaseUrl = ""
        var privacyPolicyUrl = ""
        @JvmField
        var adminPassword = ""
        var activationKeyTimeoutInSeconds = 24 * 60 * 60 // 1 day
    }

    class Mail {
        var from = ""
    }

    class Frontend {
        @JvmField
        var clientId = ""
        var clientSecret = ""
        @JvmField
        var accessTokenValiditySeconds = 4 * 60 * 60
        @JvmField
        var refreshTokenValiditySeconds = 72 * 60 * 60
        var sessionTimeout = 24 * 60 * 60 // a day
    }

    class Oauth {
        @JvmField
        var clientsFile: String? = null
        var signingKeyAlias: String? = null
        var checkingKeyAliases: List<String>? = null
        lateinit var keyStorePassword: String
        var metaTokenTimeout: String? = null
        var persistentMetaTokenTimeout: String? = null
        var enablePublicKeyVerifiers = false
    }

    class CatalogueServer {
        var isEnableAutoImport = false
        var serverUrl: String? = null
    }

    class SiteSettings {
        var hiddenSubjectFields: List<String> = listOf()
    }
}
