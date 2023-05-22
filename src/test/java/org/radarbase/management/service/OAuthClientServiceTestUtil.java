package org.radarbase.management.service;

import org.radarbase.management.service.dto.ClientDetailsDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        result.setAdditionalInformation(Map.of("dynamic_registration", "true"));
        return result;
    }
}
