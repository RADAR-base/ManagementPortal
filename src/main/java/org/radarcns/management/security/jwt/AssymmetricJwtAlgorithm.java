package org.radarcns.management.security.jwt;

import java.security.KeyPair;
import java.util.Objects;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;

public abstract class AssymmetricJwtAlgorithm implements JwtAlgorithm {
    protected final KeyPair keyPair;
    private final String algorithmName;

    protected AssymmetricJwtAlgorithm(KeyPair keyPair, String algorithmName) {
        this.keyPair = keyPair;
        this.algorithmName = algorithmName;
    }

    @Override
    public Signer getSigner() {
        return new AsymmetricKeySigner(keyPair.getPrivate(), algorithmName);
    }

    @Override
    public SignatureVerifier getVerifier() {
        return new AsymmetricKeyVerifier(keyPair.getPublic(), algorithmName);
    }

    /** Header used for encoding public keys. */
    protected abstract String getEncodedStringHeader();

    /** Footer used for encoding public keys. */
    protected abstract String getEncodedStringFooter();

    @Override
    public String getEncodedString() {
        return getEncodedStringHeader() + '\n'
                + new String(Base64.encode(keyPair.getPublic().getEncoded()))
                + '\n' + getEncodedStringFooter();
    }
}
