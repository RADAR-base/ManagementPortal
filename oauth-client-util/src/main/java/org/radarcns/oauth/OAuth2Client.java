package org.radarcns.oauth;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
 */
public class OAuth2Client {
    private URL tokenEndpoint;
    private String clientId = "";
    private String clientSecret = "";
    private final Set<String> scope = new HashSet<>();
    private OAuth2AccessTokenDetails currentToken = new OAuth2AccessTokenDetails();

    private OkHttpClient httpClient;

    public URL getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Set<String> getScope() {
        return scope;
    }

    public OAuth2Client tokenEndpoint(URL tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        return this;
    }

    public OAuth2Client clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public OAuth2Client clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public OAuth2Client addScope(String scope) {
        this.scope.add(scope);
        return this;
    }

    /**
     * Get the access token. This method will automatically request a new access token if the
     * current one is expired.
     * @return the access token
     * @throws TokenException if a new access token could not be fetched
     */
    public OAuth2AccessTokenDetails getAccessToken() throws TokenException {
        if (currentToken.isExpired()) {
            getNewToken();
        }
        return currentToken;
    }

    /**
     * Gets the stored {@link OkHttpClient} or creates one if none is stored.
     * @return the {@link OkHttpClient} instance
     */
    public OkHttpClient getHttpClient() {
        if (httpClient == null) {
            // create a client with some default settings
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }

    public OAuth2Client httpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    private void getNewToken() throws TokenException {
        // build the form to post to the token endpoint
        FormBody body = new FormBody.Builder().add("grant_type", "client_credentials")
                .add("scope", String.join(" ", scope)).build();

        String credential = Credentials.basic(getClientId(), getClientSecret());

        // build the POST request to the token endpoint with the form data
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", credential)
                .url(getTokenEndpoint())
                .post(body)
                .build();

        // make the client execute the POST request
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                currentToken = OAuth2AccessTokenDetails.getObject(response);
            } else {
                throw new TokenException("Cannot get a valid token : Response-code :"
                        + response.code() + " received when requesting token from server with "
                        + "message " + response.message());
            }

        } catch (IOException e) {
            throw new TokenException(e);
        }
    }
}
