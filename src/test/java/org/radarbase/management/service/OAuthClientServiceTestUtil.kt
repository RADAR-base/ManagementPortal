package org.radarbase.management.service

import org.radarbase.management.service.dto.ClientDetailsDTO

/**
 * Test class for the OAuthClientService class.
 *
 * @see OAuthClientService
 */
object OAuthClientServiceTestUtil {
    /**
     * Create an entity for this test.
     *
     *
     * This is a static method, as tests for other entities might also need it, if they test an
     * entity which requires the current entity.
     */
    fun createClient(): ClientDetailsDTO {
        val result = ClientDetailsDTO()
        result.clientId = "TEST_CLIENT"
        result.clientSecret = "TEST_SECRET"
        result.scope = setOf("scope-1", "scope-2")
        result.resourceIds = setOf("res-1", "res-2")
        result.autoApproveScopes = setOf("scope-1")
        result.authorizedGrantTypes = setOf(
            "password", "refresh_token",
            "authorization_code"
        )
        result.accessTokenValiditySeconds = 3600L
        result.refreshTokenValiditySeconds = 7200L
        result.authorities = setOf("AUTHORITY-1")
        val additionalInfo = LinkedHashMap<String, String>()
        additionalInfo["dynamic_registration"] = "true"
        result.additionalInformation = additionalInfo
        return result
    }
}
