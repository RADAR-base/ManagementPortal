package org.radarbase.management.security

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Test class for the SecurityUtils utility class.
 *
 * @see SecurityUtils
 */
internal class SecurityUtilsUnitTest {
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
