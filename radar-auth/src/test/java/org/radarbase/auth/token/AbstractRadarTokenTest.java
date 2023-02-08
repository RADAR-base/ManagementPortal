package org.radarbase.auth.token;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.radarbase.auth.authorization.AuthorizationOracle;
import org.radarbase.auth.authorization.EntityRelationService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.auth.authorization.Permission.MEASUREMENT_CREATE;
import static org.radarbase.auth.authorization.RoleAuthority.PARTICIPANT;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;
import static org.radarbase.auth.token.AbstractRadarToken.CLIENT_CREDENTIALS;

public class AbstractRadarTokenTest {
    private AuthorizationOracle oracle;
    private MockToken token;

    static class MockToken extends AbstractRadarToken {
        private final Set<AuthorityReference> roles = new HashSet<>();
        private final List<String> sources = new ArrayList<>();
        private final Set<String> scopes = new LinkedHashSet<>();
        private String grantType = "refresh_token";
        private String subject = "";

        @Override
        public Set<AuthorityReference> getRoles() {
            return roles;
        }

        @Override
        public Set<String> getScopes() {
            return scopes;
        }

        @Override
        public List<String> getSources() {
            return sources;
        }

        @Override
        public String getGrantType() {
            return grantType;
        }

        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getUsername() {
            return subject;
        }

        @Override
        public Date getIssuedAt() {
            return new Date();
        }

        @Override
        public Date getExpiresAt() {
            var calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 3000);
            return calendar.getTime();
        }

        @Override
        public List<String> getAudience() {
            return List.of();
        }

        @Override
        public String getToken() {
            return "";
        }

        @Override
        public String getIssuer() {
            return "";
        }

        @Override
        public String getType() {
            return "";
        }

        @Override
        public String getClientId() {
            return "";
        }

        @Override
        public String getClaimString(String name) {
            return "";
        }

        @Override
        public List<String> getClaimList(String name) {
            return List.of();
        }
    }

    public static class MockEntityRelationService implements EntityRelationService {
        private final Map<String, String> projectToOrganization;

        public MockEntityRelationService() {
            this(Map.of());
        }

        public MockEntityRelationService(Map<String, String> projectToOrganization) {
            this.projectToOrganization = projectToOrganization;
        }

        @Override
        public boolean organizationContainsProject(@NotNull String organization,
                @NotNull String project) {
            return findOrganizationOfProject(project).equals(organization);
        }

        @NotNull
        @Override
        public String findOrganizationOfProject(@NotNull String project) {
            return projectToOrganization.getOrDefault(project, "main");
        }
    }

    @BeforeEach
    public void setUp() {
        this.oracle = new AuthorizationOracle(new MockEntityRelationService());
        this.token = new MockToken();
    }

    @Test
    void notHasPermissionWithoutScope() {
        assertFalse(oracle.hasScope(token, MEASUREMENT_CREATE));
    }

    @Test
    void notHasPermissionWithoutAuthority() {
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(oracle.hasScope(token, MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsAdmin() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(SYS_ADMIN));
        assertTrue(oracle.hasScope(token, MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsUser() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "some"));
        assertTrue(oracle.hasScope(token, MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsClient() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(oracle.hasScope(token, MEASUREMENT_CREATE));
    }

    @Test
    void notHasPermissionOnProjectWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE, e -> e.project("project")));
    }

    @Test
    void notHasPermissioOnProjectnWithoutAuthority() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE, e -> e.project("project")));
    }

    @Test
    void hasPermissionOnProjectAsAdmin() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(SYS_ADMIN));
        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE, e -> e.project("project")));
    }

    @Test
    void hasPermissionOnProjectAsUser() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "project"));
        token.subject = "subject";
        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE, e -> e
                .project("project").subject("subject")));
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("otherProject")));
    }

    @Test
    void hasPermissionOnProjectAsClient() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE, e -> e.project("project")));
    }


    @Test
    void notHasPermissionOnSubjectWithoutScope() {
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("subject")));
    }

    @Test
    void notHasPermissioOnSubjectnWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("subject")));
    }

    @Test
    void hasPermissionOnSubjectAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(SYS_ADMIN));
        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("subject")));
    }

    @Test
    void hasPermissionOnSubjectAsUser() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "project"));
        token.subject = "subject";

        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("subject")));
        assertFalse(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("otherSubject")));
    }

    @Test
    void hasPermissionOnSubjectAsClient() {
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(oracle.hasPermission(token, MEASUREMENT_CREATE,
                e -> e.project("project").subject("subject")));
    }
}
