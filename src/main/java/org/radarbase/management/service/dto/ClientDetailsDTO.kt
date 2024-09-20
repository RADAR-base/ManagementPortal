package org.radarbase.management.service.dto

import javax.validation.constraints.NotNull

/**
 * Created by dverbeec on 7/09/2017.
 */
class ClientDetailsDTO {
    @NotNull
    var clientId: String? = null
    var clientSecret: String? = null
    var scope: Set<String>? = null
    var resourceIds: Set<String>? = null
    var authorizedGrantTypes: Set<String>? = null
    var autoApproveScopes: Set<String>? = null
    var accessTokenValiditySeconds: Long? = null
    var refreshTokenValiditySeconds: Long? = null
    var authorities: Set<String>? = null
    var registeredRedirectUri: Set<String>? = null
    var additionalInformation: MutableMap<String, String>? = null
}
