package org.radarbase.management.service.dto

import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Created by dverbeec on 29/08/2017.
 */
class ClientPairInfoDTO(
    baseUrl: URL,
    tokenName: String,
    tokenUrl: URL?,
    timeout: Duration,
) {
    val tokenName: String
    val tokenUrl: URL
    val baseUrl: URL
    val timeout: Long
    val timesOutAt: Instant

    /**
     * Initialize with the given refresh token.
     * @param baseUrl the base url of the platform
     * @param tokenName the refresh token
     * @param tokenUrl the refresh token
     */
    init {
        requireNotNull(tokenUrl) { "tokenUrl can not be null" }
        this.baseUrl = baseUrl
        this.tokenName = tokenName
        this.tokenUrl = tokenUrl
        this.timeout = timeout.toMillis()
        timesOutAt = Instant.now().plus(timeout)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as ClientPairInfoDTO
        return tokenName == that.tokenName &&
            tokenUrl == that.tokenUrl &&
            baseUrl == that.baseUrl &&
            timeout == that.timeout &&
            timesOutAt == that.timesOutAt
    }

    override fun hashCode(): Int = Objects.hash(baseUrl, tokenName, tokenUrl, timeout, timesOutAt)

    override fun toString(): String =
        (
            "ClientPairInfoDTO{" +
                "tokenName='" + tokenName + '\'' +
                ", tokenUrl=" + tokenUrl + '\'' +
                ", timeout=" + timeout + '\'' +
                ", timesOutAt=" + timesOutAt + '\'' +
                ", baseUrl=" + baseUrl + '}'
            )
}
