package org.radarbase.management.service.dto;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by dverbeec on 29/08/2017.
 */
public class ClientPairInfoDTO {

    private final String tokenName;

    private final URL tokenUrl;

    private final URL baseUrl;

    private final long timeout;

    private final Instant timesOutAt;


    /**
     * Initialize with the given refresh token.
     * @param baseUrl the base url of the platform
     * @param tokenName the refresh token
     * @param tokenUrl the refresh token
     */
    public ClientPairInfoDTO(URL baseUrl, String tokenName, URL tokenUrl, Duration timeout) {
        if (tokenUrl == null) {
            throw new IllegalArgumentException("tokenUrl can not be null");
        }
        this.baseUrl = baseUrl;
        this.tokenName = tokenName;
        this.tokenUrl = tokenUrl;
        this.timeout = timeout.toMillis();
        this.timesOutAt = Instant.now().plus(timeout);
    }

    public String getTokenName() {
        return tokenName;
    }

    public URL getTokenUrl() {
        return tokenUrl;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public long getTimeout() {
        return timeout;
    }

    public Instant getTimesOutAt() {
        return timesOutAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientPairInfoDTO that = (ClientPairInfoDTO) o;
        return Objects.equals(tokenName, that.tokenName)
                && Objects.equals(tokenUrl, that.tokenUrl)
                && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(timeout, that.timeout)
                && Objects.equals(timesOutAt, that.timesOutAt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(baseUrl, tokenName, tokenUrl, timeout, timesOutAt);
    }

    @Override
    public String toString() {
        return "ClientPairInfoDTO{"
                + "tokenName='" + tokenName + '\''
                + ", tokenUrl=" + tokenUrl + '\''
                + ", timeout=" + timeout + '\''
                + ", timesOutAt=" + timesOutAt + '\''
                + ", baseUrl=" + baseUrl + '}';
    }
}
