package org.radarbase.auth.security.jwk;

import java.util.Objects;

/**
 * Represents the JavaWebKey for token verification.
 */
public class JavaWebKey {

    private String alg;

    private String kty;

    private String value;


    public JavaWebKey alg(String alg) {
        this.alg = alg;
        return this;
    }

    public JavaWebKey value(String value) {
        this.value = value;
        return this;
    }

    public JavaWebKey kty(String kty) {
        this.kty = kty;
        return this;
    }

    public String getAlg() {
        return alg;
    }

    public String getValue() {
        return value;
    }

    public String getKty() {
        return kty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaWebKey that = (JavaWebKey) o;
        return Objects.equals(alg, that.alg)
                && Objects.equals(kty, that.kty)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(alg, kty, value);
    }
}
