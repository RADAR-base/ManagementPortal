package org.radarcns.management.web.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.binary.Base64;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

/**
 * Created by dverbeec on 29/06/2017.
 */
public class OAuthHelper {
    public static String VALID_TOKEN;
    public static DecodedJWT SUPER_USER_TOKEN;

    public static final String[] SCOPES = {};
    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN"};
    public static final String[] ROLES = {};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";

    static {
        try {
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For use with MockMvc
    public static RequestPostProcessor bearerToken() {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + VALID_TOKEN);
            return mockRequest;
        };
    }

    /**
     * Set up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
     * @throws Exception If anything goes wrong during setup
     */
    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream keyStream = OAuthHelper.class
                .getClassLoader().getResourceAsStream("config/keystore.jks");
        ks.load(keyStream, "radarbase".toCharArray());
        RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey("selfsigned",
                "radarbase".toCharArray());
        Certificate cert = ks.getCertificate("selfsigned");
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        keyStream.close();
        String publicKeyString = new String(new Base64().encode(publicKey.getEncoded()));
        initVars(publicKeyString, Algorithm.RSA256(publicKey, privateKey));
    }

    private static void initVars(String publicKey, Algorithm algorithm) {
        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();

        initValidToken(algorithm, exp, iat);
    }



    private static void initValidToken(Algorithm algorithm, Instant exp, Instant iat) {
        VALID_TOKEN = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience("res_ManagementPortal")
                .withSubject(USER)
                .withArrayClaim("scope", SCOPES)
                .withArrayClaim("authorities", AUTHORITIES)
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", SOURCES)
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .sign(algorithm);
        SUPER_USER_TOKEN = JWT.decode(VALID_TOKEN);
    }
}
