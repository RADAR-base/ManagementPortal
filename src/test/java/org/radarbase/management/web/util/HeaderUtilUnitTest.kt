package org.radarbase.management.web.util;

import org.junit.jupiter.api.Test;
import org.radarbase.management.web.rest.util.HeaderUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the HeaderUtil class.
 *
 * @see HeaderUtil
 */
class HeaderUtilUnitTest {

    @Test
    void pathHasLeadingSlash() {
        String path = HeaderUtil.buildPath("api", "subjects");
        assertThat(path).isEqualTo("/api/subjects");
    }

    @Test
    void nullComponentsAreIgnored() {
        String path = HeaderUtil.buildPath(null, "api", null, "subjects");
        assertThat(path).isEqualTo("/api/subjects");
    }

    @Test
    void emptyComponentsAreIgnored() {
        String path = HeaderUtil.buildPath("", "api", "", "subjects");
        assertThat(path).isEqualTo("/api/subjects");
    }

    @Test
    void charactersAreEscaped() {
        String path = HeaderUtil.buildPath("api", "subjects", "sub/1/2/3");
        assertThat(path).isEqualTo("/api/subjects/sub%2F1%2F2%2F3");
    }
}
