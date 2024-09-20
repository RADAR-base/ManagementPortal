/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.service

import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder,
) {
    private val random: Random = SecureRandom()

    /**
     * Encodes a plaintext password.
     * @param password password to encode.
     * @return encoded password.
     */
    fun encode(password: String?): String = passwordEncoder.encode(password)

    /**
     * Generates a random password that is already encoded.
     * @return encoded password.
     */
    fun generateEncodedPassword(): String = encode(generateString(ALPHANUMERIC, 30))

    /**
     * Generates a random numeric reset key.
     * @return reset key.
     */
    fun generateResetKey(): String = generateString(NUMERIC, 20)

    private fun generateString(
        allowedCharacters: IntArray,
        length: Int,
    ): String =
        random
            .ints(0, allowedCharacters.size)
            .map { i: Int -> allowedCharacters[i] }
            .limit(length.toLong())
            .collect(
                { StringBuilder(length) },
                { obj: StringBuilder, codePoint: Int -> obj.appendCodePoint(codePoint) },
            ) { obj: StringBuilder, s: StringBuilder? ->
                obj.append(
                    s,
                )
            }.toString()

    /**
     * Check that given password is strong enough, based on complexity and length.
     * @param password password to check.
     * @throws BadRequestException if the password is too weak or too long.
     */
    fun checkPasswordStrength(password: String?) {
        if (isPasswordWeak(password)) {
            throw BadRequestException(
                "Weak password. Use a password with more variety of" +
                    "numeric, alphabetical and symbol characters.",
                EntityName.Companion.USER,
                ErrorConstants.ERR_PASSWORD_TOO_WEAK,
            )
        } else if (password!!.length > 100) {
            throw BadRequestException(
                "Password too long",
                EntityName.Companion.USER,
                ErrorConstants.ERR_PASSWORD_TOO_LONG,
            )
        }
    }

    /** Check whether given password is too weak.  */
    private fun isPasswordWeak(password: String?): Boolean =
        (
            password!!.length < 8 ||
                noneInRange(password, UPPER[0], UPPER[UPPER.size - 1]) ||
                noneInRange(password, LOWER[0], LOWER[LOWER.size - 1]) ||
                noneInRange(password, NUMERIC[0], NUMERIC[NUMERIC.size - 1])
            )

    private fun noneInRange(
        str: String?,
        startInclusive: Int,
        endInclusive: Int,
    ): Boolean = str!!.chars().noneMatch { c: Int -> c >= startInclusive && c < endInclusive }

    companion object {
        val NUMERIC: IntArray
        val ALPHANUMERIC: IntArray
        private val LOWER: IntArray
        private val UPPER: IntArray

        init {
            val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val lower = upper.lowercase()
            val digits = "0123456789"
            UPPER = upper.chars().toArray()
            LOWER = lower.chars().toArray()
            NUMERIC = digits.chars().toArray()
            ALPHANUMERIC = (upper + lower + digits).chars().toArray()
        }
    }
}
