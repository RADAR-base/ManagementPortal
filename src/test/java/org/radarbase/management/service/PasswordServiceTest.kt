/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.web.rest.errors.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
internal class PasswordServiceTest {
    @Autowired
    private val passwordService: PasswordService? = null
    @Test
    fun encode() {
        Assertions.assertNotEquals("abc", passwordService!!.encode("abc"))
    }

    @Test
    fun generateEncodedPassword() {
        val pass = passwordService!!.generateEncodedPassword()
        Assertions.assertTrue(pass.length >= 30)
        Assertions.assertTrue(pass.length < 100)
        Assertions.assertDoesNotThrow { passwordService.checkPasswordStrength(pass) }
    }

    @Test
    fun generateResetKey() {
        val resetKey = passwordService!!.generateResetKey()
        Assertions.assertTrue(resetKey.length > 16)
        Assertions.assertTrue(resetKey.length < 100)
    }

    @Test
    fun checkPasswordStrength() {
        Assertions.assertDoesNotThrow { passwordService!!.checkPasswordStrength("aA1aaaaaaaa") }
        Assertions.assertThrows(BadRequestException::class.java) { passwordService!!.checkPasswordStrength("a") }
        val tooLong = ByteArray(101)
        Arrays.fill(tooLong, 'A'.code.toByte())
        Assertions.assertThrows(BadRequestException::class.java) {
            passwordService!!.checkPasswordStrength(
                "aA1" + String(
                    tooLong
                )
            )
        }
        Assertions.assertThrows(BadRequestException::class.java) { passwordService!!.checkPasswordStrength("aAaaaaaaaaa") }
        Assertions.assertThrows(BadRequestException::class.java) { passwordService!!.checkPasswordStrength("a1aaaaaaaaa") }
        Assertions.assertThrows(BadRequestException::class.java) { passwordService!!.checkPasswordStrength("aAaaaaaaaaa") }
    }
}
