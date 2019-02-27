package org.radarcns.management.security.jwt.algorithm;

import java.security.KeyPair;

import org.springframework.security.crypto.codec.Base64;

public abstract class AsymmetricalJwtAlgorithm implements JwtAlgorithm {

    protected final KeyPair keyPair;

    protected AsymmetricalJwtAlgorithm(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /** Header used for encoding public keys. */
    protected abstract String getEncodedStringHeader();

    /** Footer used for encoding public keys. */
    protected abstract String getEncodedStringFooter();

    @Override
    public String getVerifierKeyEncodedString() {
        return getEncodedStringHeader() + '\n'
                + new String(Base64.encode(keyPair.getPublic().getEncoded()))
                + '\n' + getEncodedStringFooter();
    }
}
