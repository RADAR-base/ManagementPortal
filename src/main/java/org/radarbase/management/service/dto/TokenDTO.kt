package org.radarbase.management.service.dto

import java.net.URL
import java.util.*

class TokenDTO
/**
 * Create a meta-token using refreshToken, baseUrl of platform, and privacyPolicyURL for this
 * token.
 * @param refreshToken refreshToken.
 * @param baseUrl baseUrl of the platform
 * @param privacyPolicyUrl privacyPolicyUrl for this token.
 */(val refreshToken: String, val baseUrl: URL, val privacyPolicyUrl: URL) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as TokenDTO
        return refreshToken == that.refreshToken && baseUrl == that.baseUrl && privacyPolicyUrl == that.privacyPolicyUrl
    }

    override fun hashCode(): Int {
        return Objects.hash(refreshToken, baseUrl, privacyPolicyUrl)
    }

    override fun toString(): String {
        return ("TokenDTO{"
                + "refreshToken='" + refreshToken
                + ", baseUrl=" + baseUrl
                + ", privacyPolicyUrl=" + privacyPolicyUrl
                + '}')
    }
}
