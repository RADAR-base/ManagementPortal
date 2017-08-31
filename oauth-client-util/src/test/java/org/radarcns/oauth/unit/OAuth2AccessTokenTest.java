package org.radarcns.oauth.unit;

import org.junit.Test;
import org.radarcns.oauth.OAuth2AccessToken;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dverbeec on 31/08/2017.
 */
public class OAuth2AccessTokenTest {

    @Test
    public void testNewTokenIsExpired() {
        OAuth2AccessToken token = new OAuth2AccessToken();
        assertTrue(token.isExpired());
    }

    @Test
    public void testNewTokenIsInvalid() {
        OAuth2AccessToken token = new OAuth2AccessToken();
        assertFalse(token.isValid());
    }

    @Test
    public void testTokenNotExpired() {
        OAuth2AccessToken token = new OAuth2AccessToken(null, null, 10,
            null, null, null, Instant.now().getEpochSecond(), null, null, null, null);
        assertFalse(token.isExpired());
    }
}
