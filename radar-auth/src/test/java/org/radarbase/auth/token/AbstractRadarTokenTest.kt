package org.radarbase.auth.token

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.auth.authorization.*
import org.radarbase.auth.token.RadarToken.Companion.CLIENT_CREDENTIALS
import java.time.Instant

class AbstractRadarTokenTest {
    private lateinit var oracle: AuthorizationOracle
    private lateinit var token: DataRadarToken

    private fun createMockToken() = DataRadarToken(
        roles = emptySet(),
        scopes = emptySet(),
        grantType = "refresh_token",
        expiresAt = Instant.MAX,
    )

    class MockEntityRelationService @JvmOverloads constructor(
        private val projectToOrganization: Map<String, String> = mapOf()
    ) : EntityRelationService {
        override suspend fun findOrganizationOfProject(project: String): String {
            return projectToOrganization[project] ?: "main"
        }
    }

    @BeforeEach
    fun setUp() {
        oracle = MPAuthorizationOracle(MockEntityRelationService())
        token = createMockToken()
    }

    @Test
    fun notHasPermissionWithoutScope() {
        assertFalse(oracle.hasScope(token, Permission.MEASUREMENT_CREATE))
    }

    @Test
    fun notHasPermissionWithoutAuthority() {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
        )
        assertFalse(oracle.hasScope(token, Permission.MEASUREMENT_CREATE))
    }

    @Test
    fun hasPermissionAsAdmin() {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.SYS_ADMIN))
        )
        assertTrue(oracle.hasScope(token, Permission.MEASUREMENT_CREATE))
    }

    @Test
    fun hasPermissionAsUser() {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.PARTICIPANT, "some")),
        )
        assertTrue(oracle.hasScope(token, Permission.MEASUREMENT_CREATE))
    }

    @Test
    fun hasPermissionAsClient() {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            grantType = CLIENT_CREDENTIALS
        )
        assertTrue(oracle.hasScope(token, Permission.MEASUREMENT_CREATE))
    }

    @Test
    fun notHasPermissionOnProjectWithoutScope() = runBlocking {
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project")
            )
        )
    }

    @Test
    fun notHasPermissioOnProjectnWithoutAuthority() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope())
        )
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project")
            )
        )
    }

    @Test
    fun hasPermissionOnProjectAsAdmin() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.SYS_ADMIN)),
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project")
            )
        )
    }

    @Test
    fun hasPermissionOnProjectAsUser() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.PARTICIPANT, "project")),
            subject = "subject",
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject")
            )
        )
        assertFalse(
            oracle.hasPermission(
                token, Permission.MEASUREMENT_CREATE, EntityDetails(project = "project"),
            )
        )
    }

    @Test
    fun hasPermissionOnProjectAsClient() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            grantType = CLIENT_CREDENTIALS,
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project")
            )
        )
    }

    @Test
    fun notHasPermissionOnSubjectWithoutScope() = runBlocking {
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject")
            )
        )
    }

    @Test
    fun notHasPermissioOnSubjectnWithoutAuthority() = runBlocking {
        token = token.copy(scopes = setOf(Permission.MEASUREMENT_CREATE.scope()))
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject")
            ),
        )
    }

    @Test
    fun hasPermissionOnSubjectAsAdmin() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.SYS_ADMIN)),
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject")
            )
        )
    }

    @Test
    fun hasPermissionOnSubjectAsUser() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            roles = setOf(AuthorityReference(RoleAuthority.PARTICIPANT, "project")),
            subject = "subject",
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject")
            )
        )
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "otherSubject")
            )
        )
    }

    @Test
    fun hasPermissionOnSubjectAsClient() = runBlocking {
        token = token.copy(
            scopes = setOf(Permission.MEASUREMENT_CREATE.scope()),
            grantType = CLIENT_CREDENTIALS
        )
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.MEASUREMENT_CREATE,
                EntityDetails(project = "project", subject = "subject"),
            )
        )
    }
}
