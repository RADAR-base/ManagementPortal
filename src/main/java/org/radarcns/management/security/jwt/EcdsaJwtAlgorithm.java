package org.radarcns.management.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;

public class EcdsaJwtAlgorithm extends JwtAlgorithm<ECPublicKey, ECPrivateKey> {
    public static final String ALGORITHM_NAME = "SHA256withECDSA";

    public EcdsaJwtAlgorithm(KeyPair keyPair) {
        super((ECPublicKey)keyPair.getPublic(), (ECPrivateKey)keyPair.getPrivate());
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
        return Algorithm.ECDSA256(publicKey, privateKey);
    }

    @Override
    public String getEncodedPublicKeyHeader() {
        return "-----BEGIN EC PUBLIC KEY-----";
    }

    @Override
    public String getEncodedPublicKeyFooter() {
        return "-----END EC PUBLIC KEY-----";
    }
}
