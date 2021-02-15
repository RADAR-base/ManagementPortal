/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarcns.management.service.util;

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

    /** Calculate password strength. A score of at least 40 is good. */
    public int score(String password) {
        int passedMatches = (int) Arrays.stream(patterns)
                .filter(p -> p.matcher(password).find())
                .count();

        int force = 2 * password.length() + 10 * passedMatches;

        // penality (short password)
        if (password.length() <= 8 && force > 20) {
            force = 20;
        }
        // penality (poor variety of characters)
        if (passedMatches == 1 && force > 10) {
            force = 10;
        }
        if (passedMatches == 2 && force > 30) {
            force = 30;
        }

        return force;
    }
}
