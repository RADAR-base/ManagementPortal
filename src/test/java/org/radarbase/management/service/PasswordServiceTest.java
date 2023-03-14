/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
class PasswordServiceTest {
    @Autowired
    private PasswordService passwordService;

    @Test
    void encode() {
        assertNotEquals("abc", passwordService.encode("abc"));
    }

    @Test
    void generateEncodedPassword() {
        String pass = passwordService.generateEncodedPassword();
        assertTrue(pass.length() >= 30);
        assertTrue(pass.length() < 100);
        assertDoesNotThrow(() -> passwordService.checkPasswordStrength(pass));
    }

    @Test
    void generateResetKey() {
        String resetKey = passwordService.generateResetKey();
        assertTrue(resetKey.length() > 16);
        assertTrue(resetKey.length() < 100);
    }

    @Test
    void checkPasswordStrength() {
        assertDoesNotThrow(() -> passwordService.checkPasswordStrength("aA1aaaaaaaa"));
        assertThrows(BadRequestException.class, () -> passwordService.checkPasswordStrength("a"));
        byte[] tooLong = new byte[101];
        Arrays.fill(tooLong, (byte)'A');
        assertThrows(BadRequestException.class, () ->
                passwordService.checkPasswordStrength("aA1" + new String(tooLong)));
        assertThrows(BadRequestException.class, () ->
                passwordService.checkPasswordStrength("aAaaaaaaaaa"));
        assertThrows(BadRequestException.class, () ->
                passwordService.checkPasswordStrength("a1aaaaaaaaa"));
        assertThrows(BadRequestException.class, () ->
                passwordService.checkPasswordStrength("aAaaaaaaaaa"));
    }
}
