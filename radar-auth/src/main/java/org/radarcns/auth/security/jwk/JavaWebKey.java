package org.radarcns.auth.security.jwk;

/**
 * Represents the JavaWebKey for token verification.
 */
public class JavaWebKey {

    private String alg;

    private String value;

    public JavaWebKey alg(String alg) {
        this.alg = alg;
        return this;
    }

    public JavaWebKey value(String value) {
        this.value = value;
        return this;
    }

    public String getAlg() {
        return alg;
    }

    public String getValue() {
        return value;
    }
}
