package org.radarcns.security.authorization;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import org.bouncycastle.util.io.pem.PemReader;
import org.radarcns.security.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthorizationHandler implementation for use with the WSO2 Identity Server.
 */
public class RadarAuthorizationHandler implements AuthorizationHandler {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorizationHandler.class);
    private final ServerConfig config;
    private RSAPublicKey publicKey;

    // when did we last check a given token?
    Map<String, Instant> tokenCache;

    // we check every 30 minutes if a token has not been revoked
    private static final long TOKEN_CACHE_TIME_SECONDS = 30 * 60;


    public RadarAuthorizationHandler(ServerConfig config) throws NoSuchAlgorithmException,
        IOException, InvalidKeySpecException, NotAuthorizedException {
        this.config = config;
        publicKey = publicKeyFromServer();
        if (publicKey == null) {
            throw new IOException("Unable to fetch the server's public key");
        }
        tokenCache = new HashMap<>();
    }

    @Override
    public DecodedJWT validateAccessToken(String token) throws JWTVerificationException,
            NotAuthorizedException, IOException {
        // first check the signature of the token
        Algorithm alg = Algorithm.RSA256(publicKey, null);
        JWTVerifier verifier = JWT.require(alg).build();
        DecodedJWT jwt = verifier.verify(token);

        if (tokenCache.containsKey(token) && tokenCache.get(token)
                .plusSeconds(TOKEN_CACHE_TIME_SECONDS).isAfter(Instant.now())) {
            // cache time not elapsed yet
            return jwt;
        }

        // cache time elapsed, or not cached yet, query the server for token validity
        callCheckTokenEndpoint(jwt, token);
        return jwt;
    }

    @Override
    public ServerConfig getIdentityServerConfig() {
        return config;
    }

    protected void callCheckTokenEndpoint(DecodedJWT jwt, String token) throws IOException,
        NotAuthorizedException {

        // Build a client with authorization and form data to get the token information
        OkHttpClient client = new OkHttpClient.Builder()
                    .authenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) throws
                                    IOException {
                            String credential = Credentials.basic(config.getUsername(), config
                                        .getPassword());
                            return response.request().newBuilder().header("Authorization",
                                        credential).build();
                        }
                    }).build();

        RequestBody form = new FormBody.Builder()
                    .add("token", token)
                    .build();

        Request request = new Request.Builder()
                    .url(config.getTokenValidationEndpoint())
                    .post(form)
                    .build();

        log.debug("Checking validity of token at URL " + config.getTokenValidationEndpoint());

        // Call the endpoint
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new NotAuthorizedException("Identity server returned code " + response + ", "
                + "with body: " + response.body().toString(),
                javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
        }

        // Parse the response
        JsonParser parser = new JsonFactory().createParser(response.body().charStream());
        TypeReference<Map<String, Object>> ref = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> tokenInfo = new ObjectMapper().reader().readValue(parser, ref);

        checkTokenResponse(jwt, tokenInfo);
    }

    protected void checkTokenResponse(DecodedJWT jwt, Map<String, Object> tokenInfo) throws
        NotAuthorizedException {
        // Do some checks on the data structure
        if (tokenInfo.containsKey("error")) {
            throw new NotAuthorizedException("Received error from identity server: "
                + tokenInfo.get("error").toString(),
                javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
        }

        String[] expectedFields = {"aud", "scope", "authorities", "user_name", "exp", "jti"};
        for (String field : expectedFields) {
            if (!tokenInfo.containsKey(field)) {
                throw new NotAuthorizedException("Expected field " + field
                    + " to be present in the returned JSON",
                    javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
            }
        }

        // Check the values of the token_type and active field
        Long exp = new Long((Integer) tokenInfo.get("exp"));
        if (Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
            throw new NotAuthorizedException("Token is no longer valid",
                javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
        }

        if (!tokenInfo.get("jti").equals(jwt.getId())) {
            throw new NotAuthorizedException("Embedded ID of supplied token does not match ID of "
                + "server response", javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
        }
    }

    private RSAPublicKey publicKeyFromServer() throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException, NotAuthorizedException {
        log.debug("Getting the public key at " + config.getPublicKeyEndpoint());

        URLConnection connection = new URL(config.getPublicKeyEndpoint()).openConnection();
        connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
        final InputStream inputStream = connection.getInputStream();
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(inputStream);
        final TypeReference<Map<String, Object>> typeReference =
            new TypeReference<Map<String, Object>>() {
            };
        Map<String, Object> publicKeyInfo = new ObjectMapper().reader().
            readValue(parser, typeReference);

        // we expect RSA algorithm, and deny to trust the public key otherwise
        // see also https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
        if (!publicKeyInfo.get("alg").equals("SHA256withRSA")) {
            throw new NotAuthorizedException("The identity server reported the following "
                + "signing algorithm: " + publicKeyInfo.get("alg") + ". Expected SHA256withRSA.",
                javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN));
        }

        String keyString = (String) publicKeyInfo.get("value");
        return publicKeyFromString(keyString);
    }

    private RSAPublicKey publicKeyFromString(String keyString) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        log.debug("Parsing public key: " + keyString);
        byte[] keyBytes = new PemReader(new StringReader(keyString)).readPemObject().getContent();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }
}
