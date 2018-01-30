package org.radarcns.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.radarcns.exception.TokenException;

/**
 * Class for handling OAuth2 client credentials grant with the RADAR platform's ManagementPortal.
 *
 * <p>Altough it is designed with the ManagementPortal in mind, any identity server based on the
 * Spring OAuth library and using JWT as a token should be compatible. The {@link #getAccessToken()}
 * method provides access to the {@link OAuth2AccessTokenDetails} instance, and will request a new
 * access token if the current one is expired. It will throw a {@link TokenException} if anything
 * went wrong. So to get the actual token you will call
 * {@code client.getAccessToken().getAccessToken()}. This token is in JWT format and can be
 * parsed by a JWT library of your preference. Note: by default, the public key endpoint on
 * ManagementPortal is located at {@code /oauth/token_key}.</p>
 *
 * <p>See the test cases for this class for examples on usage. Also see
 * {@link OAuth2AccessTokenDetails} for more info on how to use it.</p>
 *
 * @implSpec this class is not thread-safe.
 */
// using builder pattern.
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class OAuth2Client {
    private final URL tokenEndpoint;
    private final Set<String> scope;
    private final OkHttpClient httpClient;
    private final String clientCredentials;

    private OAuth2AccessTokenDetails currentToken;

    private OAuth2Client(URL endpoint, String clientCredentials, Set<String> scopes,
            OAuth2AccessTokenDetails token, OkHttpClient client) {
        this.tokenEndpoint = endpoint;
        this.clientCredentials = clientCredentials;
        this.scope = scopes;
        this.currentToken = token;
        httpClient = client;
    }

    public URL getTokenEndpoint() {
        return tokenEndpoint;
    }

    public Set<String> getScope() {
        return scope;
    }

    /**
     * Get the access token. This method will automatically request a new access token if the
     * current one is expired.
     * @return the access token
     * @throws TokenException if a new access token could not be fetched
     */
    public OAuth2AccessTokenDetails getAccessToken() throws TokenException {
        if (currentToken.isExpired()) {
            requestToken();
        }
        return currentToken;
    }

    /**
     * Check whether given token is still valid for the given amount of time.
     * @param timeStillValid duration that the token should still be valid.
     * @return {@code true} if the token is valid for given duration, {@code false} otherwise.
     */
    public boolean isTokenValidFor(Duration timeStillValid) {
        return currentToken.isValid()
            && Instant.now().plus(timeStillValid).isBefore(currentToken.getExpiryDate());
    }

    private void requestToken() throws TokenException {
        // build the form to post to the token endpoint
        FormBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("scope", String.join(" ", scope))
                .build();

        // build the POST request to the token endpoint with the form data
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", clientCredentials)
                .url(getTokenEndpoint())
                .post(body)
                .build();

        // make the client execute the POST request
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new TokenException("No response from server");
                }
                currentToken = OAuth2AccessTokenDetails.getObject(responseBody.string());
            } else {
                throw new TokenException("Cannot get a valid token : Response-code :"
                        + response.code() + " received when requesting token from server with "
                        + "message " + response.message());
            }

        } catch (IOException e) {
            throw new TokenException(e);
        }
    }

    /** Builder for an OAuth2 client. The endpoint and credentials settings are mandatory. */
    public static class Builder {
        private URL tokenEndpoint;
        private final Set<String> scopeSet = new HashSet<>();
        private OAuth2AccessTokenDetails currentToken = new OAuth2AccessTokenDetails();
        private OkHttpClient okHttpClient;
        private String clientCredentials;

        public Builder endpoint(URL url) {
            this.tokenEndpoint = url;
            return this;
        }

        public Builder endpoint(URL mpBaseUrl, String tokenPath) throws MalformedURLException {
            tokenEndpoint = new URL(mpBaseUrl, tokenPath);
            return this;
        }

        public Builder credentials(String id, String secret) {
            clientCredentials = Credentials.basic(id, secret);
            return this;
        }

        public Builder scopes(String... scopes) {
            scopeSet.addAll(Arrays.asList(scopes));
            return this;
        }

        public Builder token(OAuth2AccessTokenDetails token) {
            currentToken = token;
            return this;
        }

        public Builder httpClient(OkHttpClient client) {
            okHttpClient = client;
            return this;
        }

        /** Build an OAuth2Client based on the settings given. */
        public OAuth2Client build() {
            if (clientCredentials == null) {
                throw new IllegalStateException("Client credentials missing");
            }
            if (tokenEndpoint == null) {
                throw new IllegalStateException("Token endpoint missing");
            }
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            }
            return new OAuth2Client(tokenEndpoint, clientCredentials, scopeSet, currentToken,
                okHttpClient);
        }
    }
}
