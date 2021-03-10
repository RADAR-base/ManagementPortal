/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.service.util;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Password utility. */
public class PasswordUtil {
    private final Pattern[] patterns = {
            // numeric
            Pattern.compile("[0-9]"),
            // letter
            Pattern.compile("[a-zA-Z]"),
            // symbol
            Pattern.compile("[^a-zA-Z0-9]")
    };

    /** Check whether given password is too weak. */
    public boolean isPasswordWeak(String password) {
        return !Arrays.stream(patterns).allMatch(p -> p.matcher(password).find())
                || password.length() < 8;
    }
}
