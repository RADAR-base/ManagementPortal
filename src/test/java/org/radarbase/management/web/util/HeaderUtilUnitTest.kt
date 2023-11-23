package org.radarbase.management.web.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.radarbase.management.web.rest.util.HeaderUtil

/**
 * Test class for the HeaderUtil class.
 *
 * @see HeaderUtil
 */
internal class HeaderUtilUnitTest {
    @Test
    fun pathHasLeadingSlash() {
        val path = HeaderUtil.buildPath("api", "subjects")
        Assertions.assertThat(path).isEqualTo("/api/subjects")
    }

    @Test
    fun nullComponentsAreIgnored() {
        val path = HeaderUtil.buildPath(null, "api", null, "subjects")
        Assertions.assertThat(path).isEqualTo("/api/subjects")
    }

    @Test
    fun emptyComponentsAreIgnored() {
        val path = HeaderUtil.buildPath("", "api", "", "subjects")
        Assertions.assertThat(path).isEqualTo("/api/subjects")
    }

    @Test
    fun charactersAreEscaped() {
        val path = HeaderUtil.buildPath("api", "subjects", "sub/1/2/3")
        Assertions.assertThat(path).isEqualTo("/api/subjects/sub%2F1%2F2%2F3")
    }
}
