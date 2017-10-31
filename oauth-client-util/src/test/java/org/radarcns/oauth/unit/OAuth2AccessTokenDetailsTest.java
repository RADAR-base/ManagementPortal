package org.radarcns.oauth.unit;

import org.junit.Test;
import org.radarcns.oauth.OAuth2AccessTokenDetails;

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
    public void testTokenNotExpired() {
        OAuth2AccessTokenDetails token = new OAuth2AccessTokenDetails();
        token.setIssueDate(Instant.now().getEpochSecond());
        token.setExpiresIn(30);
        assertFalse(token.isExpired());
    }
}
