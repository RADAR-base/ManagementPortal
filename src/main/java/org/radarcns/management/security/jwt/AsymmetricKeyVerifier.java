package org.radarcns.management.security.jwt;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

/**
 * Class that verifies signatures from asymmetric keys for use in Spring Security.
 */
public class AsymmetricKeyVerifier implements SignatureVerifier {

    private static Logger logger = LoggerFactory.getLogger(AsymmetricKeyVerifier.class);
    private final PublicKey publicKey;
    private final String algorithm;

    public AsymmetricKeyVerifier(PublicKey publicKey, String algorithm) {
        this.publicKey = publicKey;
        this.algorithm = algorithm;
    }

    @Override
    public void verify(byte[] content, byte[] sig) {
        logger.debug("Verifying with algorithm {}: content {} with sig {}", algorithm,
                Base64.getEncoder().encodeToString(content),
                Base64.getEncoder().encodeToString(sig));
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(content);

            if (!signature.verify(sig)) {
                throw new InvalidSignatureException("Signature did not match content");
            }
        } catch (GeneralSecurityException ex) {
            logger.debug("Cannot verify content", ex);
            throw new SignatureException("An error occurred verifying the signature", ex);
        }
    }

    @Override
    public String algorithm() {
        return algorithm;
    }
}
