/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.service;

import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import static org.radarbase.management.web.rest.errors.EntityName.USER;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PASSWORD_TOO_LONG;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PASSWORD_TOO_WEAK;

@Service
public class PasswordService {
    public static final int[] NUMERIC;
    public static final int[] ALPHANUMERIC;
    private static final int[] LOWER;
    private static final int[] UPPER;

    static {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase(Locale.ROOT);
        String digits = "0123456789";
        UPPER = upper.chars().toArray();
        LOWER = lower.chars().toArray();
        NUMERIC = digits.chars().toArray();
        ALPHANUMERIC = (upper + lower + digits).chars().toArray();
    }

    private final PasswordEncoder passwordEncoder;
    private final Random random = new SecureRandom();

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Encodes a plaintext password.
     * @param password password to encode.
     * @return encoded password.
     */
    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Generates a random password that is already encoded.
     * @return encoded password.
     */
    public String generateEncodedPassword() {
        return encode(generateString(ALPHANUMERIC, 30));
    }

    /**
     * Generates a random numeric reset key.
     * @return reset key.
     */
    public String generateResetKey() {
        return generateString(NUMERIC, 20);
    }

    private String generateString(int[] allowedCharacters, int length) {
        return random.ints(0, allowedCharacters.length)
                .map(i -> allowedCharacters[i])
                .limit(length)
                .collect(() -> new StringBuilder(length),
                        StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Check that given password is strong enough, based on complexity and length.
     * @param password password to check.
     * @throws BadRequestException if the password is too weak or too long.
     */
    public void checkPasswordStrength(String password) {
        if (isPasswordWeak(password)) {
            throw new BadRequestException("Weak password. Use a password with more variety of"
                    + "numeric, alphabetical and symbol characters.", USER, ERR_PASSWORD_TOO_WEAK);
        } else if (password.length() > 100) {
            throw new BadRequestException("Password too long", USER, ERR_PASSWORD_TOO_LONG);
        }
    }

    /** Check whether given password is too weak. */
    private boolean isPasswordWeak(String password) {
        return password.length() < 12;
    }

    private boolean noneInRange(String str, int startInclusive, int endInclusive) {
        return str.chars().noneMatch(c -> c >= startInclusive && c < endInclusive);
    }
}
