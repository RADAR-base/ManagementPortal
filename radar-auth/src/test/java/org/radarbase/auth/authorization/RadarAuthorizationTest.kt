package org.radarbase.auth.authorization

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.auth.authorization.MPAuthorizationOracle.Permissions.permissionMatrix
import org.radarbase.auth.jwt.JwtTokenVerifier.Companion.toRadarToken
import org.radarbase.auth.token.AbstractRadarTokenTest.MockEntityRelationService
import org.radarbase.auth.token.RadarToken
import org.radarbase.auth.util.TokenTestUtils

/**
 * Created by dverbeec on 25/09/2017.
 */
internal class RadarAuthorizationTest {
    private lateinit var oracle: AuthorizationOracle
    @BeforeEach
    fun setUp() {
        oracle = MPAuthorizationOracle(
            MockEntityRelationService()
        )
    }

    @Test
    fun testCheckPermissionOnProject() = runBlocking {
        val project = "PROJECT1"
        // let's get all permissions a project admin has
        val token: RadarToken = TokenTestUtils.PROJECT_ADMIN_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT1")

        permissionMatrix.asSequence()
            .filter { (_, roles) -> RoleAuthority.PROJECT_ADMIN in roles }
            .map { (p, _) -> p }
            .distinct()
            .forEach { p -> assertTrue(oracle.hasPermission(token, p, entity)) }

        permissionMatrix.asSequence()
            .filter { (_, roles) -> RoleAuthority.PROJECT_ADMIN !in roles }
            .map { (p, _) -> p }
            .distinct()
            .forEach { p ->
                assertFalse(
                    oracle.hasPermission(token, p, entity),
                    String.format("Token should not have permission %s on project %s", p, project),
                )
            }
    }

    @Test
    fun testCheckPermissionOnOrganization() = runBlocking {
        val token = TokenTestUtils.ORGANIZATION_ADMIN_TOKEN.toRadarToken()
        val entity = EntityDetails(organization = "main")
        assertFalse(
            oracle.hasPermission(token, Permission.ORGANIZATION_CREATE, entity),
            "Token should not be able to create organization"
        )
        assertTrue(oracle.hasPermission(token, Permission.PROJECT_CREATE, entity))
        assertTrue(
            oracle.hasPermission(
                token,
                Permission.SUBJECT_CREATE,
                entity.copy(project = "PROJECT1"),
            ),
        )
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.PROJECT_CREATE,
                EntityDetails(organization = "otherOrg"),
            ),
            "Token should not be able to create project in other organization",
        )
        assertFalse(
            oracle.hasPermission(
                token,
                Permission.SUBJECT_CREATE,
                EntityDetails(organization = "otherOrg"),
            ),
            "Token should not be able to create subject in other organization"
        )
    }

    @Test
    fun testCheckPermission() = runBlocking {
        val token: RadarToken = TokenTestUtils.SUPER_USER_TOKEN.toRadarToken()
        for (p in Permission.values()) {
            assertTrue(oracle.hasGlobalPermission(token, p))
        }
    }

    @Test
    fun testCheckPermissionOnSelf() = runBlocking {
        // this token is participant in PROJECT2
        val token: RadarToken = TokenTestUtils.PROJECT_ADMIN_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT2", subject = token.subject)
        listOf(
            Permission.MEASUREMENT_CREATE,
            Permission.MEASUREMENT_READ,
            Permission.SUBJECT_UPDATE,
            Permission.SUBJECT_READ
        ).forEach { p -> assertTrue(oracle.hasPermission(token, p, entity)) }
    }

    @Test
    fun testCheckPermissionOnOtherSubject() = runBlocking {
        // is only participant in project2, so should not have any permission on another subject
        val entity = EntityDetails(project = "PROJECT2", subject = "other-subject")
        // this token is participant in PROJECT2
        val token: RadarToken = TokenTestUtils.PROJECT_ADMIN_TOKEN.toRadarToken()
        Permission.values().forEach { p ->
            assertFalse(
                oracle.hasPermission(token, p, entity),
                "Token should not have permission $p on another subject",
            )
        }
    }

    @Test
    fun testCheckPermissionOnSubject() = runBlocking {
        // project admin should have all permissions on subject in his project
        // this token is participant in PROJECT2
        val token: RadarToken = TokenTestUtils.PROJECT_ADMIN_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT1", subject = "some-subject")
        Permission.values()
            .asSequence()
            .filter { p -> p.entity === Permission.Entity.SUBJECT }
            .forEach { p ->
                assertTrue(oracle.hasPermission(token, p, entity))
            }
    }

    @Test
    fun testMultipleRolesInProjectToken() = runBlocking {
        val token: RadarToken = TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT2", subject = "some-subject")
        Permission.values()
            .asSequence()
            .filter { p -> p.entity === Permission.Entity.SUBJECT }
            .forEach { p ->
                assertTrue(oracle.hasPermission(token, p, entity))
            }
    }

    @Test
    fun testCheckPermissionOnSource() = runBlocking {
        // this token is participant in PROJECT2
        val token: RadarToken = TokenTestUtils.PROJECT_ADMIN_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT2", subject = "some-subject", source = "source-1")
        Permission.values()
            .forEach { p: Permission ->
                assertFalse(
                    oracle.hasPermission(token, p, entity),
                    "Token should not have permission $p on another subject",
                )
            }
    }

    @Test
    fun testCheckPermissionOnOwnSource() = runBlocking {
        val token: RadarToken = TokenTestUtils.MULTIPLE_ROLES_IN_PROJECT_TOKEN.toRadarToken()
        val entity = EntityDetails(project = "PROJECT2", subject = token.subject, source = "source-1")
        Permission.values()
            .asSequence()
            .filter { p -> p.entity === Permission.Entity.MEASUREMENT }
            .forEach { p ->
                assertTrue(oracle.hasPermission(token, p, entity))
            }
    }

    @Test
    fun testScopeOnlyToken() = runBlocking {
        val token: RadarToken = TokenTestUtils.SCOPE_TOKEN.toRadarToken()
        // test that we can do the things we have a scope for
        val entities = listOf(
            EntityDetails.global,
            EntityDetails(project = "PROJECT1"),
            EntityDetails(project = "PROJECT1", subject = ""),
            EntityDetails(project = "PROJECT1", subject = "", source = ""),
        )
        listOf(
            Permission.SUBJECT_READ,
            Permission.SUBJECT_CREATE,
            Permission.PROJECT_READ,
            Permission.MEASUREMENT_CREATE
        ).forEach { p ->
            entities.forEach { e ->
                assertTrue(oracle.hasPermission(token, p, e))
            }
        }

        // test we can do nothing else, for each of the checkPermission methods
        Permission.values()
            .asSequence()
            .filter { p -> p.scope() !in token.scopes }
            .forEach { p ->
                entities.forEach { e ->
                    assertFalse(
                        oracle.hasPermission(token, p, e),
                        "Permission $p is granted but not in scope.",
                    )
                }
            }
    }
}
