package org.radarbase.management.service;

import org.radarbase.management.service.dto.ClientDetailsDTO;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Test class for the OAuthClientService class.
 *
 * @see OAuthClientService
 */
public final class OAuthClientServiceTestUtil {
    private OAuthClientServiceTestUtil() {
        // utility class
    }

    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it, if they test an
     * entity which requires the current entity.</p>
     */
    public static ClientDetailsDTO createClient() {
        ClientDetailsDTO result = new ClientDetailsDTO();
        result.setClientId("TEST_CLIENT");
        result.setClientSecret("TEST_SECRET");
        result.setScope(Set.of("scope-1", "scope-2"));
        result.setResourceIds(Set.of("res-1", "res-2"));
        result.setAutoApproveScopes(Set.of("scope-1"));
        result.setAuthorizedGrantTypes(Set.of("password", "refresh_token",
                "authorization_code"));
        result.setAccessTokenValiditySeconds(3600L);
        result.setRefreshTokenValiditySeconds(7200L);
        result.setAuthorities(Set.of("AUTHORITY-1"));
        var additionalInfo = new LinkedHashMap<String, String>();
        additionalInfo.put("dynamic_registration", "true");
        result.setAdditionalInformation(additionalInfo);
        return result;
    }
}
