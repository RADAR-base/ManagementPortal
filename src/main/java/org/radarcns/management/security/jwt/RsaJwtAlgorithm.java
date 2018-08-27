package org.radarcns.management.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;

public class RsaJwtAlgorithm extends JwtAlgorithm<RSAPublicKey, RSAPrivateKey> {
    private static final String ALGORITHM_NAME = "SHA256withRSA";

    public RsaJwtAlgorithm(KeyPair keyPair) {
        super((RSAPublicKey)keyPair.getPublic(), (RSAPrivateKey)keyPair.getPrivate());
    }

    @Override
    public Signer getSigner() {
        return new AsymmetricKeySigner(privateKey, ALGORITHM_NAME);
    }

    @Override
    public SignatureVerifier getVerifier() {
        return new AsymmetricKeyVerifier(publicKey, ALGORITHM_NAME);
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.RSA256(publicKey, privateKey);
    }

    @Override
    public String getEncodedPublicKeyHeader() {
        return "-----BEGIN PUBLIC KEY-----";
    }

    @Override
    public String getEncodedPublicKeyFooter() {
        return "-----END PUBLIC KEY-----";
    }
}
