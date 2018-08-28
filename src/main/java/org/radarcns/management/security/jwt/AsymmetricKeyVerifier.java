package org.radarcns.management.security.jwt;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

/**
 * Class that verifies signatures from asymmetric keys for use in Spring Security.
 */
public class AsymmetricKeyVerifier implements SignatureVerifier {

    private final PublicKey publicKey;
    private final String algorithm;

    public AsymmetricKeyVerifier(PublicKey publicKey, String algorithm) {
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
                throw new InvalidSignatureException("Signature did not match content");
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
