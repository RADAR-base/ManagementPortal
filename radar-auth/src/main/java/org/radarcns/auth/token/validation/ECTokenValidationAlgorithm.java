package org.radarcns.auth.token.validation;

import com.auth0.jwt.algorithms.Algorithm;

import java.security.interfaces.ECPublicKey;

public class ECTokenValidationAlgorithm extends AbstractTokenValidationAlgorithm {

    @Override
    public String getJwtAlgorithm() {
        return "SHA256withECDSA";
    }

    @Override
    public String getKeyHeader() {
        return "-----BEGIN EC PUBLIC KEY-----";
    }

    @Override
    public Algorithm getAlgorithm(String publicKey) {
        return Algorithm.ECDSA256((ECPublicKey) parseKey(publicKey), null);
    }

    @Override
    protected String getKeyFactoryType() {
        return "EC";
    }
}
