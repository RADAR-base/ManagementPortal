package org.radarcns.management.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.radarcns.management.service.dto.ClientDetailsDTO;

public class OauthClientServiceTest {

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
        result.setScope(Arrays.asList("scope-1", "scope-2").stream().collect(Collectors.toSet()));
        result.setResourceIds(Arrays.asList("res-1", "res-2").stream().collect(Collectors.toSet()));
        result.setAutoApproveScopes(Arrays.asList("scope-1").stream().collect(Collectors.toSet()));
        result.setAuthorizedGrantTypes(Arrays.asList("password", "refresh_token",
                "authorization_code").stream().collect(Collectors.toSet()));
        result.setAccessTokenValiditySeconds(3600L);
        result.setRefreshTokenValiditySeconds(7200L);
        result.setAuthorities(Arrays.asList("AUTHORITY-1").stream().collect(Collectors.toSet()));
        result.setAdditionalInformation(new HashMap<>());
        result.getAdditionalInformation().put("dynamic_registration", "true");
        return result;
    }
}
