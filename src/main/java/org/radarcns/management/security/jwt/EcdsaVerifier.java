package org.radarcns.management.security.jwt;

import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;

public class EcdsaVerifier implements SignatureVerifier {

    private final ECPublicKey publicKey;
    private final String algorithm;

    public EcdsaVerifier(ECPublicKey publicKey) {
        this(publicKey, EcdsaSigner.DEFAULT_ALGORITHM);
    }

    public EcdsaVerifier(ECPublicKey publicKey, String algorithm) {
        this.publicKey = publicKey;
        this.algorithm = algorithm;
    }

    @Override
    public void verify(byte[] content, byte[] sig) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(content);

            if (!signature.verify(sig)) {
                throw new InvalidSignatureException("EC Signature did not match content");
            }
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("An error occured verifying the signature", ex);
        }
    }

    @Override
    public String algorithm() {
        return algorithm;
    }
}
