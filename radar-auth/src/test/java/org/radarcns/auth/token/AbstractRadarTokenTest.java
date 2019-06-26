package org.radarcns.auth.token;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.SYS_ADMIN;
import static org.radarcns.auth.authorization.Permission.MEASUREMENT_CREATE;
import static org.radarcns.auth.token.AbstractRadarToken.CLIENT_CREDENTIALS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class AbstractRadarTokenTest {
    static class MockToken extends AbstractRadarToken {
        private final Map<String, List<String>> roles = new HashMap<>();
        private final List<String> sources = new ArrayList<>();
        private final List<String> scopes = new ArrayList<>();
        private String grantType = "refresh_token";
        private final List<String> authorities = new ArrayList<>();
        private String subject = null;

        @Override
        public Map<String, List<String>> getRoles() {
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
        public Date getIssuedAt() {
            return null;
        }

        @Override
        public Date getExpiresAt() {
            return null;
        }

        @Override
        public List<String> getAudience() {
            return null;
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
    }

    @Test
    public void notHasPermissionWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    public void notHasPermissionWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    public void hasPermissionAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.authorities.add(SYS_ADMIN);
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    public void hasPermissionAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.roles.put("some", Collections.singletonList(PARTICIPANT));
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    public void hasPermissionAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermission(MEASUREMENT_CREATE));
    }

    @Test
    public void notHasPermissionOnProjectWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    public void notHasPermissioOnProjectnWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    public void hasPermissionOnProjectAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.authorities.add(SYS_ADMIN);
        assertTrue(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }

    @Test
    public void hasPermissionOnProjectAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.roles.put("project", Collections.singletonList(PARTICIPANT));
        assertTrue(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
        assertFalse(token.hasPermissionOnProject(MEASUREMENT_CREATE, "otherProject"));
    }

    @Test
    public void hasPermissionOnProjectAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermissionOnProject(MEASUREMENT_CREATE, "project"));
    }


    @Test
    public void notHasPermissionOnSubjectWithoutScope() {
        MockToken token = new MockToken();
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    public void notHasPermissioOnSubjectnWithoutAuthority() {
        MockToken token = new MockToken();
        token.scopes.add("MEASUREMENT_CREATE");
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    public void hasPermissionOnSubjectAsAdmin() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.authorities.add(SYS_ADMIN);
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }

    @Test
    public void hasPermissionOnSubjectAsUser() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.roles.put("project", Collections.singletonList(PARTICIPANT));
        token.subject = "subject";
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "otherProject", "subject"));
        assertFalse(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "otherSubject"));
    }

    @Test
    public void hasPermissionOnSubjectAsClient() {
        MockToken token = new MockToken();
        token.scopes.add(MEASUREMENT_CREATE.scopeName());
        token.grantType = CLIENT_CREDENTIALS;
        assertTrue(token.hasPermissionOnSubject(MEASUREMENT_CREATE, "project", "subject"));
    }
}
