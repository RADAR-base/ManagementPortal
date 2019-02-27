package org.radarcns.management.security.jwt.algorithm;

import com.auth0.jwt.algorithms.Algorithm;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public class EcdsaJwtAlgorithm extends AsymmetricalJwtAlgorithm {
    /** ECDSA JWT algorithm. */
    public EcdsaJwtAlgorithm(KeyPair keyPair) {
        super(keyPair);
        if (!(keyPair.getPrivate() instanceof ECPrivateKey)) {
            throw new IllegalArgumentException(
                    "Cannot make EcdsaJwtAlgorithm with " + keyPair.getPrivate().getClass());
        }
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.ECDSA256(
                (ECPublicKey)keyPair.getPublic(),
                (ECPrivateKey)keyPair.getPrivate());
    }

    @Override
    public String getEncodedStringHeader() {
        return "-----BEGIN EC PUBLIC KEY-----";
    }

    @Override
    public String getEncodedStringFooter() {
        return "-----END EC PUBLIC KEY-----";
    }
}
