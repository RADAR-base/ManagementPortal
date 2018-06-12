package org.radarcns.management.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a SignatureVerifier that can accept multiple SignatureVerifier. If the signature
 * passes any of the supplied verifiers, the signature is assumed to be valid.
 */
public class MultiVerifier implements SignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(MultiVerifier.class);
    private final List<SignatureVerifier> verifiers = new LinkedList<>();

    /**
     * Construct a MultiVerifier from the given list of SignatureVerifiers.
     * @param verifiers the list of verifiers to use
     */
    public MultiVerifier(List<SignatureVerifier> verifiers) {
        this.verifiers.addAll(verifiers);
    }

    @Override
    public void verify(byte[] content, byte[] signature) {
        for (SignatureVerifier verifier : verifiers) {
            try {
                verifier.verify(content, signature);
                return;
            } catch (RuntimeException ex) {
                log.debug("Verifier {} with implementation {} could not verify the signature",
                        verifier.toString(), verifier.getClass().toString());
            }
        }
        throw new SignatureException("Signature could not be verified by any of the registered "
                + "verifiers");
    }

    @Override
    public String algorithm() {
        return null;
    }
}
