package org.radarbase.management.security.jwt.algorithm;

import java.security.KeyPair;
import java.util.Base64;

import org.radarbase.auth.security.jwk.JavaWebKey;

public abstract class AsymmetricalJwtAlgorithm implements JwtAlgorithm {

    protected final KeyPair keyPair;

    protected AsymmetricalJwtAlgorithm(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /** Header used for encoding public keys. */
    protected abstract String getEncodedStringHeader();

    /** Footer used for encoding public keys. */
    protected abstract String getEncodedStringFooter();

    /** The family of cryptographic algorithms used with the key. */
    protected abstract String getKeyType();

    @Override
    public String getVerifierKeyEncodedString() {
        return getEncodedStringHeader() + '\n'
                + new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()))
                + '\n' + getEncodedStringFooter();
    }

    @Override
    public JavaWebKey getJwk() {
        return new JavaWebKey()
                .alg(this.getAlgorithm().getName())
                .kty(this.getKeyType())
                .value(this.getVerifierKeyEncodedString());
    }
}
