package org.radarcns.management.web.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.management.config.LocalKeystoreConfig;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 29/06/2017.
 */
public class OAuthHelper {
    public static String validEcToken;
    public static DecodedJWT superUserToken;
    public static String validRsaToken;

    public static final String TEST_KEYSTORE_PASSWORD = "radarbase";
    public static final String TEST_SIGNKEY_ALIAS = "radarbase-managementportal-ec";
    public static final String TEST_CHECKKEY_ALIAS = "radarbase-managementportal-rsa";
    public static final String[] SCOPES = allScopes();
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

    /**
     * Create a request post processor that adds a valid bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    public static RequestPostProcessor bearerToken() {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + validEcToken);
            return mockRequest;
        };
    }

    /**
     * Create a request post processor that adds a valid RSA bearer token to requests for use with
     * MockMVC.
     * @return the request post processor
     */
    public static RequestPostProcessor rsaBearerToken() {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + validRsaToken);
            return mockRequest;
        };
    }

    /**
     * Set up a keypair for signing the tokens, initialize all kinds of different tokens for tests.
     * @throws Exception If anything goes wrong during setup
     */
    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        InputStream keyStream = OAuthHelper.class
                .getClassLoader().getResourceAsStream("config/keystore.p12");
        ks.load(keyStream, TEST_KEYSTORE_PASSWORD.toCharArray());

        // get the EC keypair for signing
        ECPrivateKey privateKey = (ECPrivateKey) ks.getKey(TEST_SIGNKEY_ALIAS,
                TEST_KEYSTORE_PASSWORD.toCharArray());
        Certificate cert = ks.getCertificate(TEST_SIGNKEY_ALIAS);
        ECPublicKey publicKey = (ECPublicKey) cert.getPublicKey();

        validEcToken = createValidToken(Algorithm.ECDSA256(publicKey, privateKey));
        superUserToken = JWT.decode(validEcToken);

        // also get an RSA keypair to test accepting multiple keys
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) ks.getKey(TEST_CHECKKEY_ALIAS,
                TEST_KEYSTORE_PASSWORD.toCharArray());
        RSAPublicKey rsaPublicKey = (RSAPublicKey) ks.getCertificate(TEST_CHECKKEY_ALIAS)
                .getPublicKey();
        validRsaToken = createValidToken(Algorithm.RSA256(rsaPublicKey, rsaPrivateKey));
        keyStream.close();
    }

    /**
     * Helper method to initialize an authentication filter for use in test classes.
     *
     * @return an initialized JwtAuthenticationFilter
     */
    public static JwtAuthenticationFilter createAuthenticationFilter() {
        return new JwtAuthenticationFilter(new TokenValidator(
                new LocalKeystoreConfig(TEST_KEYSTORE_PASSWORD, Arrays.asList(TEST_SIGNKEY_ALIAS,
                        TEST_CHECKKEY_ALIAS))));
    }

    private static String createValidToken(Algorithm algorithm) {
        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();
        return JWT.create()
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
                .withClaim("grant_type", "password")
                .sign(algorithm);
    }

    private static String[] allScopes() {
        return Permission.allPermissions().stream()
                .map(Permission::scopeName)
                .collect(Collectors.toList()).toArray(
                        new String[Permission.allPermissions().size()]);
    }
}
