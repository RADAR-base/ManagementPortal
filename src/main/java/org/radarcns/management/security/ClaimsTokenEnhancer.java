package org.radarcns.management.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.management.domain.Device;
import org.radarcns.management.repository.PatientRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            Map<String, Object> additionalInfo = new HashMap<>();
            Set<String> currentUserAuthorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
//            additionalInfo.put("authorities" , currentUserAuthorities);
            if(currentUserAuthorities.contains(AuthoritiesConstants.PARTICIPANT)) {
                String userName = null;
                if (authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                    userName = springSecurityUser.getUsername();
                } else if (authentication.getPrincipal() instanceof String) {
                    userName = (String) authentication.getPrincipal();
                }
                if(userName!=null) {
                    List<Device> assignedDevices = patientRepository
                        .findDevicesByPatientLogin(userName);

                    List<String> deviceIds = assignedDevices.stream()
                        .map(Device::getDevicePhysicalId).collect(Collectors.toList());
                    additionalInfo.put("devices", deviceIds);

                }
            }
            ((DefaultOAuth2AccessToken) accessToken)
                .setAdditionalInformation(additionalInfo);
        }
        return accessToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
