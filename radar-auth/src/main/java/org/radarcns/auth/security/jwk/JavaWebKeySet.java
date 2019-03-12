package org.radarcns.auth.security.jwk;

import java.util.List;

public class JavaWebKeySet {

    private List<JavaWebKey> keys;

    public JavaWebKeySet(List<JavaWebKey> keys) {
        this.keys = keys;
    }

    public List<JavaWebKey> getKeys() {
        return keys;
    }
}
