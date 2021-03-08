package org.radarbase.auth.token.validation;

import com.auth0.jwt.algorithms.Algorithm;

import java.security.interfaces.RSAPublicKey;

public class RSATokenValidationAlgorithm extends AbstractTokenValidationAlgorithm {
    @Override
    protected String getKeyFactoryType() {
        return "RSA";
    }

    @Override
    public String getJwtAlgorithm() {
        return "SHA256withRSA";
    }

    @Override
    public String getKeyHeader() {
        return "-----BEGIN PUBLIC KEY-----";
    }

    @Override
    public Algorithm getAlgorithm(String publicKey) {
        return Algorithm.RSA256((RSAPublicKey) parseKey(publicKey), null);
    }
}
