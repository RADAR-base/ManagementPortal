package org.radarbase.management.security;

import org.radarbase.auth.authorization.AuthorizationOracle;
import org.radarbase.auth.authorization.Permission;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.domain.Role;
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
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.radarbase.auth.jwt.JwtTokenVerifier.GRANT_TYPE_CLAIM;
import static org.radarbase.auth.jwt.JwtTokenVerifier.ROLES_CLAIM;
import static org.radarbase.auth.jwt.JwtTokenVerifier.SOURCES_CLAIM;

@Component
public class ClaimsTokenEnhancer implements TokenEnhancer, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ClaimsTokenEnhancer.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuthorizationOracle authorizationOracle;

    @Value("${spring.application.name}")
    private String appName;

    private static final String GRANT_TOKEN_EVENT = "GRANT_ACCESS_TOKEN";

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
        OAuth2Authentication authentication) {
        logger.debug("Enhancing token of authentication {}" , authentication);

        Map<String, Object> additionalInfo = new HashMap<>();

        String userName = authentication.getName();

        if (authentication.getPrincipal() instanceof Principal
            || authentication.getPrincipal() instanceof UserDetails) {
            // add the 'sub' claim in accordance with JWT spec
            additionalInfo.put("sub", userName);

            Optional.ofNullable(userRepository.findOneByLogin(userName))
                .ifPresent(user -> {
                var roles = user.roles.stream()
                    .map(role -> {
                var auth = role.authority.name;
                return switch (role.getRole().getScope()) {
                    case GLOBAL -> auth;
                    case ORGANIZATION -> role.organization.name
                    + ":" + auth;
                    case PROJECT -> role.project.getProjectName()
                    + ":" + auth;
                };
            })
                .toList();
                additionalInfo.put(ROLES_CLAIM, roles);

                // Do not grant scopes that cannot be given to a user.
                Set<String> currentScopes = accessToken.getScope();
                Set<String> newScopes = currentScopes.stream()
                    .filter(scope -> {
                Permission permission = Permission.ofScope(scope);
                var roleAuthorities = user.roles.stream()
                    .map(Role::getRole)
                    .collect(Collectors.toCollection(() ->
                EnumSet.noneOf(RoleAuthority.class)));
                return authorizationOracle.mayBeGranted(roleAuthorities,
                    permission);
            })
                .collect(Collectors.toCollection(TreeSet::new));

                if (!newScopes.equals(currentScopes)) {
                    ((DefaultOAuth2AccessToken) accessToken).setScope(newScopes);
                }
            });

            List<Source> assignedSources = subjectRepository.findSourcesBySubjectLogin(userName);

            List<String> sourceIds = assignedSources.stream()
                .map(s -> s.sourceId.toString())
            .toList();
            additionalInfo.put(SOURCES_CLAIM, sourceIds);
        }
        // add iat and iss optional JWT claims
        additionalInfo.put("iat", Instant.now().getEpochSecond());
        additionalInfo.put("iss", appName);
        additionalInfo.put(GRANT_TYPE_CLAIM,
            authentication.getOAuth2Request().getGrantType());
        ((DefaultOAuth2AccessToken) accessToken)
            .setAdditionalInformation(additionalInfo);

        // HACK: since all granted tokens need to pass here, we can use this point to create an
        // audit event for a granted token, there is an open issue about oauth2 audit events in
        // spring security but it has been inactive for a long time:
        // https://github.com/spring-projects/spring-security-oauth/issues/223
        Map<String, Object> auditData = auditData(accessToken, authentication);
        auditEventRepository.add(new AuditEvent(userName, GRANT_TOKEN_EVENT,
            auditData));
        logger.info("[{}] for {}: {}", GRANT_TOKEN_EVENT, userName, auditData);

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
