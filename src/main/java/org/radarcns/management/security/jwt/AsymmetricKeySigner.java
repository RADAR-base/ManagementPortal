package org.radarcns.management.security.jwt;

import java.security.PrivateKey;
import org.springframework.security.jwt.crypto.sign.Signer;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;

/**
 * Class that creates signatures from asymmetric keys for use in Spring Security.
 */
public class AsymmetricKeySigner implements Signer {
    private final PrivateKey privateKey;
    private final String algorithm;

    public AsymmetricKeySigner(PrivateKey privateKey, String signingAlgorithm) {
        this.privateKey = privateKey;
        this.algorithm = signingAlgorithm;
    }

    @Override
    public byte[] sign(byte[] bytes) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(bytes);
            return signature.sign();
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("Could not provide a signature", ex);
        }
    }

    @Override
    public String algorithm() {
        return algorithm;
    }
}
