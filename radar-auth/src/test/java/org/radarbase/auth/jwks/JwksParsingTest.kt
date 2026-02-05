package org.radarbase.auth.jwks

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JwksParsingTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun testParseEncryptionKey() {
        val keyJson = """{"kid":"ZJ1262ous7vH3NPLOig_vD6Hi3mryOB3urc95lpCekU","kty":"RSA","alg":"RSA-OAEP","use":"enc","x5c":["MIICozCCAYsCBgGcJ7oa8zANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDDApyYWRhci1iYXNlMB4XDTI2MDIwNDA4MTUzM1oXDTM2MDIwNDA4MTcxM1owFTETMBEGA1UEAwwKcmFkYXItYmFzZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALTorcZ/Kg7pqrAeJXosnOSCYYOLWK/3QYf7diQz1ChPOkvfwt4iC+5g/jvrh0aktVOowZB8KEkxYD09kPlJWyhBoqnw5wMXWKRbybr9hPpkdKzKiwtK7PHST72JOZdl1NRUTWDIUmLDLjMYNpFqeEA6U5bFBqdvdoePqvYNieqS7WPQ/p6FAEGWNZOgR9+cNRiSqwNkzyCOdIofP16kXkQl4Qh602/OZuo8pkofkbVDjb0oUBrqDaOYfjBg0EDvBsW/aubdXu20cTLoU0JZBV/pRLwrbkaZprSxPgqPGgdMAv0JJWOBbdjgKpbC+FdUhqPk6+VRDRKFr1Nyuo4u8YcCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAGTb0UstW4uiKxSUXghUHTVC0PqrFEv4hKTylbn1rkUsZ5n9D5vLCAVpcSsmB/Wfzy4EJm6Nh0mly4cJuxl2O+BU5T8rJgaKqwpHBw+1sB4G7Hf3rE3p50pb2h2CGJYpVwM2bh9lcyBx7n9GsIVAfWa7fKmPTi7IQwvnfPDSRMb28TqVqSZGjub0Q+8kxInYdy+XbDTY7p6APpXZeGIY+/LuOdyxT187QZ3TAOAroQ/8b0WSva66I1dZm5n7En7c6lveJc6rpY/9KHTBEUmksGLqWOCPjd/HqfrFMd2qTIWXhVe1XbpZuE6aSzJ4Dvj6g6l6HPqiRqOVAyHj2gaspRw=="],"x5t":"Ms1lEmN34jjar741cp6AWYvCUBs","x5t#S256":"HY035SOCoD2vdt-uduYOkdk3-0Gyg3P4izu7xfBHqZs","n":"tOitxn8qDumqsB4leiyc5IJhg4tYr_dBh_t2JDPUKE86S9_C3iIL7mD-O-uHRqS1U6jBkHwoSTFgPT2Q-UlbKEGiqfDnAxdYpFvJuv2E-mR0rMqLC0rs8dJPvYk5l2XU1FRNYMhSYsMuMxg2kWp4QDpTlsUGp292h4-q9g2J6pLtY9D-noUAQZY1k6BH35w1GJKrA2TPII50ih8_XqReRCXhCHrTb85m6jymSh-RtUONvShQGuoNo5h-MGDQQO8Gxb9q5t1e7bRxMuhTQlkFX-lEvCtuRpmmtLE-Co8aB0wC_QklY4Ft2OAqlsL4V1SGo-Tr5VENEoWvU3K6ji7xhw","e":"AQAB"}"""
        val key = json.decodeFromString<JsonWebKey>(keyJson)
        assertInstanceOf(RSAJsonWebKey::class.java, key)
        val rsaKey = key as RSAJsonWebKey
        assertEquals("RSA-OAEP", rsaKey.alg)
        assertEquals("enc", rsaKey.use)

        assertEquals(null, rsaKey.keySize())
    }

    @Test
    fun testParseEcEncryptionKey() {
        val keyJson = """{"kid":"some-kid","kty":"EC","alg":"ECDH-ES","use":"enc","crv":"P-256","x":"abcd","y":"cdef"}"""
        val key = json.decodeFromString<JsonWebKey>(keyJson)
        assertInstanceOf(ECDSAJsonWebKey::class.java, key)
        val ecKey = key as ECDSAJsonWebKey
        assertEquals("ECDH-ES", ecKey.alg)
        assertEquals("enc", ecKey.use)

        assertEquals(null, ecKey.curve())
    }
}
