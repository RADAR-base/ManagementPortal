package org.radarcns.management.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;

/**
 * Encodes a signing and verification algorithm for JWT.
 */
public abstract class JwtAlgorithm<PU extends PublicKey, PR extends PrivateKey> {
    protected final PU publicKey;
    protected final PR privateKey;

    protected JwtAlgorithm(PU publicKey, PR privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /** Signer to sign JWTs with. */
    public abstract Signer getSigner();
    /** Verifier to verify JWTs with. */
    public abstract SignatureVerifier getVerifier();
    /** Auth0 Algorithm used in JWTs. */
    public abstract Algorithm getAlgorithm();

    /** Header used for encoding public keys. */
    protected abstract String getEncodedPublicKeyHeader();
    /** Footer used for encoding public keys. */
    protected abstract String getEncodedPublicKeyFooter();

    /** Encoded public key for storage or transmission. */
    public String getEncodedPublicKey() {
        return getEncodedPublicKeyHeader() + '\n'
                + new String(Base64.encode(publicKey.getEncoded()))
                + '\n' + getEncodedPublicKeyFooter();
    }
}
