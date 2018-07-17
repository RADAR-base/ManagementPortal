package org.radarcns.management.security.jwt;

import org.springframework.security.jwt.crypto.sign.Signer;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;

/**
 * Class that creates ECDSA signatures for use in Spring Security.
 */
public class EcdsaSigner implements Signer {

    public static final String DEFAULT_ALGORITHM = "SHA256withECDSA";
    private final ECPrivateKey privateKey;
    private final String algorithm;

    public EcdsaSigner(ECPrivateKey privateKey) {
        this(privateKey, DEFAULT_ALGORITHM);
    }

    public EcdsaSigner(ECPrivateKey privateKey, String signingAlgorithm) {
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
