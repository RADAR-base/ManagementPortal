package org.radarcns.management.security;

import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            Map<String, Object> additionalInfo = new HashMap<>();
            Set<String> currentUserAuthorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            String userName = null;
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }

            Optional<User> optUser = userRepository.findOneByLogin(userName);
            if (optUser.isPresent()) {
                List<Role> roles = optUser.get().getRoles().stream().collect(Collectors.toList());
                additionalInfo.put("roles", roles);
            }

            if(currentUserAuthorities.contains(AuthoritiesConstants.PARTICIPANT)) {
                if(userName!=null) {
                    List<Source> assignedSources = subjectRepository
                        .findSourcesBySubjectLogin(userName);

                    List<String> sourceIds = assignedSources.stream()
                        .map(Source::getSourceId).collect(Collectors.toList());
                    additionalInfo.put("sources", sourceIds);

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
