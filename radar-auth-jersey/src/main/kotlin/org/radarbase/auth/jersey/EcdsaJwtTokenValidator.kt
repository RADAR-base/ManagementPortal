/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import org.radarcns.auth.exception.ConfigurationException
import org.radarbase.auth.jersey.impl.JwtAuth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

class EcdsaJwtTokenValidator constructor(@Context private val config: AuthConfig) : AuthValidator {
    private val verifiers: List<JWTVerifier>
    init {
        val algorithms = mutableListOf<Algorithm>()

        config.jwtECPublicKeys?.let { keys ->
            algorithms.addAll(keys.map { Algorithm.ECDSA256(parseKey(it, "EC") as ECPublicKey, null) })
        }
        config.jwtRSAPublicKeys?.let { keys ->
            algorithms.addAll(keys.map { Algorithm.RSA256(parseKey(it, "RSA") as RSAPublicKey, null) })
        }

        config.jwtKeystorePath?.let { keyStorePathString ->
            algorithms.add(try {
                val pkcs12Store = KeyStore.getInstance("pkcs12")
                val keyStorePath = Paths.get(keyStorePathString)
                pkcs12Store.load(Files.newInputStream(keyStorePath), config.jwtKeystorePassword?.toCharArray())
                val publicKey: ECPublicKey = pkcs12Store.getCertificate(config.jwtKeystoreAlias).publicKey as ECPublicKey
                Algorithm.ECDSA256(publicKey, null)
            } catch (ex: Exception) {
                throw IllegalStateException("Failed to initialize JWT ECDSA public key", ex)
            })
        }

        if (algorithms.isEmpty()) {
            throw ConfigurationException("No verification algorithms given")
        } else {
            logger.info("Verifying JWTs with ${algorithms.size} algorithms")
        }

        verifiers = algorithms.map { algorithm ->
            val builder = JWT.require(algorithm)
                    .withAudience(config.jwtResourceName)
            config.jwtIssuer?.let {
                builder.withIssuer(it)
            }
            builder.build()
        }
    }

    private fun parseKey(publicKey: String, algorithm: String): PublicKey {
        var trimmedKey = publicKey.replace(Regex("-----BEGIN ([A-Z]+ )?PUBLIC KEY-----"), "")
        trimmedKey = trimmedKey.replace(Regex("-----END ([A-Z]+ )?PUBLIC KEY-----"), "")
        trimmedKey = trimmedKey.trim()
        logger.info("Using following public key for algorithm $algorithm: \n$trimmedKey")
        try {
            val keyBytes = Base64.getDecoder().decode(trimmedKey)
            val spec = X509EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance(algorithm)
            return kf.generatePublic(spec)
        } catch (ex: Exception) {
            throw ConfigurationException(ex)
        }
    }

    override fun verify(token: String, request: ContainerRequestContext): Auth? {
        val project = request.getHeaderString("RADAR-Project")

        for (verifier in verifiers) {
            try {
                val decodedJwt = verifier.verify(token)

                return JwtAuth(project, decodedJwt)
            } catch (ex: SignatureVerificationException) {
                // try next verifier
            } catch (ex: AlgorithmMismatchException) {
                // try next verifier
            } catch (ex: JWTVerificationException) {
                logger.warn("JWT verification exception", ex)
                return null
            }
        }
        return null
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(EcdsaJwtTokenValidator::class.java)
    }
}
