package org.radarbase.oauth.unit;

import org.junit.Test;
import org.radarbase.exception.TokenException;
import org.radarbase.oauth.OAuth2AccessTokenDetails;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dverbeec on 31/08/2017.
 */
public class OAuth2AccessTokenDetailsTest {

    @Test
    public void testNewTokenIsExpired() {
        OAuth2AccessTokenDetails token = new OAuth2AccessTokenDetails();
        assertTrue(token.isExpired());
    }

    @Test
    public void testNewTokenIsInvalid() {
        OAuth2AccessTokenDetails token = new OAuth2AccessTokenDetails();
        assertFalse(token.isValid());
    }

    @Test
    public void testTokenNotExpired() throws TokenException {
        String body =
                "{\"expires_in\":30"
                + ",\"iat\":" + Instant.now().getEpochSecond()
                + ",\"access_token\":\"abcdef\"}";
        OAuth2AccessTokenDetails token = OAuth2AccessTokenDetails.getObject(body);
        assertFalse(token.isExpired());
    }
}
