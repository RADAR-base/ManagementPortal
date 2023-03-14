package org.radarbase.auth.token;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.auth.authorization.RoleAuthority.PARTICIPANT;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;
import static org.radarbase.auth.authorization.Permission.MEASUREMENT_CREATE;
import static org.radarbase.auth.token.AbstractRadarToken.CLIENT_CREDENTIALS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AbstractRadarTokenTest {
    static class MockToken extends AbstractRadarToken {
        private final Set<AuthorityReference> roles = new HashSet<>();
        private final List<String> sources = new ArrayList<>();
        private final List<String> scopes = new ArrayList<>();
        private String grantType = "refresh_token";
        private final List<String> authorities = new ArrayList<>();
        private String subject = null;

        @Override
        public Set<AuthorityReference> getRoles() {
            return roles;
        }

        @Override
        public List<String> getAuthorities() {
            return authorities;
        }

        @Override
        public List<String> getScopes() {
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
            return null;
        }

        @Override
        public Date getExpiresAt() {
            return null;
        }

        @Override
        public List<String> getAudience() {
            return List.of();
        }

        @Override
        public String getToken() {
            return null;
        }

        @Override
        public String getIssuer() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public String getClientId() {
            return null;
        }

        @Override
        public String getClaimString(String name) {
            return null;
        }

        @Override
        public List<String> getClaimList(String name) {
            return List.of();
        }
    }

    @Test
    void notHasPermissionWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    void notHasPermissionWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.authorities.add(SYS_ADMIN.authority());
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "some"));
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    void hasPermissionAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    void notHasPermissionOnProjectWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    void notHasPermissioOnProjectnWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    void hasPermissionOnProjectAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.authorities.add(SYS_ADMIN.authority());
        assertTrue(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    void hasPermissionOnProjectAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "project"));
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "otherProject"));
    }

    @Test
    void hasPermissionOnProjectAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }


    @Test
    void notHasPermissionOnSubjectWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    void notHasPermissioOnSubjectnWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    void hasPermissionOnSubjectAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.authorities.add(SYS_ADMIN.authority());
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    void hasPermissionOnSubjectAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.roles.add(new AuthorityReference(PARTICIPANT, "project"));
        token.subject = "subject";
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "otherProject", "subject"));
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "otherSubject"));
    }

    @Test
    void hasPermissionOnSubjectAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scope());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }
}
