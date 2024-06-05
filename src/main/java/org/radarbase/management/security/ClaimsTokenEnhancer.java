package org.radarbase.management.security;

import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.radarbase.auth.token.JwtRadarToken;
import org.radarbase.management.domain.Source;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ClaimsTokenEnhancer.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Value("${spring.application.name}")
    private String appName;

    private static final String GRANT_TOKEN_EVENT = "GRANT_ACCESS_TOKEN";

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
            OAuth2Authentication authentication) {
        logger.debug("Enhancing token {} with authentication {}" , accessToken, authentication);

        Map<String, Object> additionalInfo = new HashMap<>();

        String principalName = authentication.getName();

        if (authentication.getPrincipal() instanceof Principal ||
                authentication.getPrincipal() instanceof UserDetails) {
            // add the 'sub' claim in accordance with JWT spec
            additionalInfo.put("sub", principalName);

            userRepository.findOneByLogin(principalName)
                    .ifPresent(user -> {
                        List<String> roles = user.getRoles().stream()
                                .filter(role -> role.getProject() != null)
                                .map(role -> role.getProject().getProjectName() + ":"
                                        + role.getAuthority().getName())
                                .collect(Collectors.toList());
                        additionalInfo.put(JwtRadarToken.ROLES_CLAIM, roles);
                    });

            List<Source> assignedSources = subjectRepository.findSourcesBySubjectLogin(principalName);

            List<String> sourceIds = assignedSources.stream()
                    .map(s -> s.getSourceId().toString())
                    .collect(Collectors.toList());
            additionalInfo.put(JwtRadarToken.SOURCES_CLAIM, sourceIds);
        }
        // add iat and iss optional JWT claims
        additionalInfo.put("iat", Instant.now().getEpochSecond());
        additionalInfo.put("iss", appName);
        additionalInfo.put(JwtRadarToken.GRANT_TYPE_CLAIM,
                authentication.getOAuth2Request().getGrantType());
        ((DefaultOAuth2AccessToken) accessToken)
                .setAdditionalInformation(additionalInfo);

        // HACK: since all granted tokens need to pass here, we can use this point to create an
        // audit event for a granted token, there is an open issue about oauth2 audit events in
        // spring security but it has been inactive for a long time:
        // https://github.com/spring-projects/spring-security-oauth/issues/223
        Map<String, Object> auditData = auditData(accessToken, authentication);
        auditEventRepository.add(new AuditEvent(principalName, GRANT_TOKEN_EVENT,
                auditData));
        logger.info("[{}] for {}: {}", GRANT_TOKEN_EVENT, principalName, auditData);

        return accessToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // nothing to do for now
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
        return result;
    }
}
