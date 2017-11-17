package org.radarcns.management.security;

import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Value("${spring.application.name}")
    private String appName;

    private final static String GRANT_TOKEN_EVENT = "GRANT_ACCESS_TOKEN";

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {

        Map<String, Object> additionalInfo = new HashMap<>();

        String userName = SecurityUtils.getUserName(authentication);

        if(userName != null) {
            // add the 'sub' claim in accordance with JWT spec
            additionalInfo.put("sub", userName);

            Optional<User> optUser = userRepository.findOneByLogin(userName);
            if (optUser.isPresent()) {
                List<String> roles = optUser.get().getRoles().stream()
                        .filter(role -> Objects.nonNull(role.getProject()))
                        .map(role -> role.getProject().getProjectName() + ":"
                                + role.getAuthority().getName())
                        .collect(Collectors.toList());
                additionalInfo.put("roles", roles);

            }

            List<Source> assignedSources = subjectRepository.findSourcesBySubjectLogin(userName);

            List<String> sourceIds = assignedSources.stream()
                    .map(s -> s.getSourceId().toString())
                    .collect(Collectors.toList());
            additionalInfo.put("sources", sourceIds);
        }
        // add iat and iss optional JWT claims
        additionalInfo.put("iat", Instant.now().getEpochSecond());
        additionalInfo.put("iss", appName);
        ((DefaultOAuth2AccessToken) accessToken)
            .setAdditionalInformation(additionalInfo);

        // HACK: since all granted tokens need to pass here, we can use this point to create an
        // audit event for a granted token, there is an open issue about oauth2 audit events in
        // spring security but it has been inactive for a long time:
        // https://github.com/spring-projects/spring-security-oauth/issues/223
        auditEventRepository.add(new AuditEvent(userName, GRANT_TOKEN_EVENT,
                auditData(accessToken, authentication)));

        return accessToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private Map<String, Object> auditData(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        result.put("tokenType", accessToken.getTokenType());
        result.put("scope", String.join(", ", accessToken.getScope()));
        result.put("expiresIn", Integer.toString(accessToken.getExpiresIn()));
        result.putAll(accessToken.getAdditionalInformation());
        OAuth2Request request = authentication.getOAuth2Request();
        result.put("clientId", request.getClientId());
        result.put("grantType", request.getGrantType());
        if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
            OAuth2AuthenticationDetails details =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            result.put("remoteAddress", details.getRemoteAddress());
        }
        return result;
    }
}
