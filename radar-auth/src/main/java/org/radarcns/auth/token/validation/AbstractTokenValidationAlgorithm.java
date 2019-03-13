package org.radarcns.auth.token.validation;

import org.radarcns.auth.exception.ConfigurationException;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public abstract class AbstractTokenValidationAlgorithm implements TokenValidationAlgorithm {
    /**
     * The key factory type for keys that this algorithm can parse.
     * @return the key factory type
     */
    protected abstract String getKeyFactoryType();

    /**
     * Parse a public key in PEM format.
     * @param publicKey the public key to parse
     * @return a PublicKey object representing the supplied public key
     */
    protected PublicKey parseKey(String publicKey) {
        String trimmedKey = publicKey.replaceAll("-----BEGIN ([A-Z]+ )?PUBLIC KEY-----", "");
        trimmedKey = trimmedKey.replaceAll("-----END ([A-Z]+ )?PUBLIC KEY-----", "");
        trimmedKey = trimmedKey.trim();

        try {
            byte[] decodedPublicKey = Base64.getDecoder().decode(trimmedKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedPublicKey);
            KeyFactory kf = KeyFactory.getInstance(getKeyFactoryType());
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }
}
