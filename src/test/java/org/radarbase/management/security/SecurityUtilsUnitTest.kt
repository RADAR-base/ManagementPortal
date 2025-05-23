package org.radarbase.management.security

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Test class for the SecurityUtils utility class.
 *
 * @see SecurityUtils
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class SecurityUtilsUnitTest : BasePostgresIntegrationTest() {
    @Test
    fun testGetCurrentUserLogin() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken(
            "admin",
            "admin"
        )
        SecurityContextHolder.setContext(securityContext)
        val login = SecurityUtils.currentUserLogin
        Assertions.assertThat(login).isEqualTo("admin")
    }
}
