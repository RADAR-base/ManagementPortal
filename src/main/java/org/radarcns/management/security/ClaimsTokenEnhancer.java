package org.radarcns.management.security;

import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.Instant;
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

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {

        Map<String, Object> additionalInfo = new HashMap<>();

        String userName = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            userName = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            userName = (String) authentication.getPrincipal();
        }

        if(userName!=null) {
            // add the 'sub' claim in accordance with JWT spec
            additionalInfo.put("sub", userName);

            Optional<User> optUser = userRepository.findOneByLogin(userName);
            if (optUser.isPresent()) {
                List<String> roles = optUser.get().getRoles().stream()
                    .filter(role -> role.getProject() != null)
                    .map(role -> role.getProject().getProjectName() + ":"
                        + role.getAuthority().getName())
                    .collect(Collectors.toList());
                additionalInfo.put("roles", roles);

            }

            List<Source> assignedSources = subjectRepository
                .findSourcesBySubjectLogin(userName);

            List<String> sourceIds = assignedSources.stream()
                .map(s -> s.getSourceId().toString()).collect(Collectors.toList());
            additionalInfo.put("sources", sourceIds);
        }
        // add iat and iss optional JWT claims
        additionalInfo.put("iat", Instant.now().getEpochSecond());
        additionalInfo.put("iss", appName);
        ((DefaultOAuth2AccessToken) accessToken)
            .setAdditionalInformation(additionalInfo);

        return accessToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
