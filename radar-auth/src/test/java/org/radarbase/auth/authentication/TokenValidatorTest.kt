package org.radarbase.auth.authentication

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.jwks.JwkAlgorithmParser
import org.radarbase.auth.jwks.JwksTokenVerifierLoader
import org.radarbase.auth.jwks.RSAPEMCertificateParser
import org.radarbase.auth.util.TokenTestUtils
import org.radarbase.auth.util.TokenTestUtils.WIREMOCK_PORT
import java.util.List

/**
 * Created by dverbeec on 24/04/2017.
 */
internal class TokenValidatorTest {
    private var validator: TokenValidator? = null

    /**
     * Set up a stub public key endpoint and initialize a TokenValidator object.
     *
     */
    @BeforeEach
    fun setUp() {
        wireMockServer!!.stubFor(
            WireMock.get(WireMock.urlEqualTo(TokenTestUtils.PUBLIC_KEY_PATH))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(TokenTestUtils.PUBLIC_KEY_BODY)
                )
        )
        val algorithmParser = JwkAlgorithmParser(List.of(RSAPEMCertificateParser()))
        val verifierLoader = JwksTokenVerifierLoader(
            "http://localhost:" + WIREMOCK_PORT + TokenTestUtils.PUBLIC_KEY_PATH,
            "unit_test",
            algorithmParser
        )
        validator = TokenValidator(List.of(verifierLoader))
    }

    @AfterEach
    fun reset() {
        wireMockServer!!.resetAll()
    }

    @Test
    fun testValidToken() {
        validator!!.validateBlocking(TokenTestUtils.VALID_RSA_TOKEN)
    }

    @Test
    fun testIncorrectAudienceToken() {
        Assertions.assertThrows(
            TokenValidationException::class.java
        ) { validator!!.validateBlocking(TokenTestUtils.INCORRECT_AUDIENCE_TOKEN) }
    }

    @Test
    fun testExpiredToken() {
        Assertions.assertThrows(
            TokenValidationException::class.java
        ) { validator!!.validateBlocking(TokenTestUtils.EXPIRED_TOKEN) }
    }

    @Test
    fun testIncorrectAlgorithmToken() {
        Assertions.assertThrows(
            TokenValidationException::class.java
        ) { validator!!.validateBlocking(TokenTestUtils.INCORRECT_ALGORITHM_TOKEN) }
    }

    companion object {
        private var wireMockServer: WireMockServer? = null
        @BeforeAll
        fun loadToken() {
            wireMockServer = WireMockServer(
                WireMockConfiguration()
                    .port(WIREMOCK_PORT)
            )
            wireMockServer!!.start()
        }

        @AfterAll
        fun tearDown() {
            wireMockServer!!.stop()
        }
    }
}
