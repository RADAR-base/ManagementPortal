package org.radarcns.auth.token.validation;

import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.auth.exception.ConfigurationException;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

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
        try (PemReader pemReader = new PemReader(new StringReader(publicKey))) {
            byte[] keyBytes = pemReader.readPemObject().getContent();
            pemReader.close();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(getKeyFactoryType());
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }
}
