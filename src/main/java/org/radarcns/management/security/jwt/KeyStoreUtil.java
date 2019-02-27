package org.radarcns.management.security.jwt;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

import javax.annotation.Nullable;

import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.LoggerFactory;


public class KeyStoreUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(KeyStoreUtil.class);

    /**
     * Returns extracted {@link Algorithm} from the KeyPair.
     * @param keyPair to find algorithm.
     * @return extracted algorithm.
     */
    public static Algorithm getAlgorithmFromKeyPair(KeyPair keyPair) {
        JwtAlgorithm alg = getJwtAlgorithm(keyPair);
        if (alg == null) {
            throw new IllegalArgumentException("KeyPair type "
                    + keyPair.getPrivate().getAlgorithm() + " is unknown.");
        }
        return  alg.getAlgorithm();
    }

    /**
     * Get the JWT algorithm to sign or verify JWTs with.
     * @param keyPair key pair for signing/verifying.
     * @return algorithm or {@code null} if the key type is unknown.
     */
    public static @Nullable JwtAlgorithm getJwtAlgorithm(@Nullable KeyPair keyPair) {

        if (keyPair == null) {
            return null;
        }
        PrivateKey privateKey = keyPair.getPrivate();

        if (privateKey instanceof ECPrivateKey) {
            return new EcdsaJwtAlgorithm(keyPair);
        } else if (privateKey instanceof RSAPrivateKey) {
            return new RsaJwtAlgorithm(keyPair);
        } else {
            logger.warn("No JWT algorithm found for key type {}", privateKey.getClass());
            return null;
        }
    }
}
