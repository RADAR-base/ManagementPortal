package org.radarcns.management.security.jwt;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.crypto.sign.Signer;

/**
 * Class that creates signatures from asymmetric keys for use in Spring Security.
 */
public class AsymmetricKeySigner implements Signer {
    private static final Logger logger = LoggerFactory.getLogger(AsymmetricKeySigner.class);

    private final PrivateKey privateKey;
    private final String algorithm;

    public AsymmetricKeySigner(PrivateKey privateKey, String signingAlgorithm) {
        this.privateKey = privateKey;
        this.algorithm = signingAlgorithm;
    }

    @Override
    public byte[] sign(byte[] bytes) {
        try {
            Signature signature = Signature.getInstance(algorithm, new BouncyCastleProvider());
            signature.initSign(privateKey);
            signature.update(bytes);
            byte[] result = signature.sign();
            logger.debug("Signing with algorithm {}: content {} with sig {}", algorithm,
                    Base64.getEncoder().encodeToString(bytes), Base64.getEncoder()
                            .encodeToString(result));
            return result;
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("Could not provide a signature", ex);
        }
    }

    @Override
    public String algorithm() {
        return algorithm;
    }
}
