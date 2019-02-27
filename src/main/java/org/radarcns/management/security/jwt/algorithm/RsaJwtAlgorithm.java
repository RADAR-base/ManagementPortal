package org.radarcns.management.security.jwt.algorithm;

import com.auth0.jwt.algorithms.Algorithm;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RsaJwtAlgorithm extends AsymmetricalJwtAlgorithm {
    /** RSA JWT algorithm. */
    public RsaJwtAlgorithm(KeyPair keyPair) {
        super(keyPair);
        if (!(keyPair.getPrivate() instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException(
                    "Cannot make RsaJwtAlgorithm with " + keyPair.getPrivate().getClass());
        }
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.RSA256(
                (RSAPublicKey)keyPair.getPublic(),
                (RSAPrivateKey)keyPair.getPrivate());
    }

    @Override
    public String getEncodedStringHeader() {
        return "-----BEGIN PUBLIC KEY-----";
    }

    @Override
    public String getEncodedStringFooter() {
        return "-----END PUBLIC KEY-----";
    }
}
