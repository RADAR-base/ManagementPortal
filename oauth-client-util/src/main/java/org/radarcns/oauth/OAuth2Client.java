package org.radarcns.oauth;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.radarcns.exception.TokenException;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Class for handling OAuth2 client credentials grant with the RADAR platform's ManagementPortal.
 * Altough it is designed with the ManagementPortal in mind, any identity server based on the Spring
 * OAuth library and using JWT as a token should be compatible. The {@link #getAccessToken()}
 * method provides access to the {@link OAuth2AccessTokenDetails} instance, and will request a new
 * access token if the current one is expired. It will throw a {@link TokenException} if anything
 * went wrong. So to get the actual token you will call
 * <code>client.getAccessToken().getAccessToken()</code>. This token is in JWT format and can be
 * parsed by a JWT library of your preference. Note: by default, the public key endpoint on
 * ManagementPortal is located at <code>/oauth/token_key</code>.
 *
 * See the test cases for this class for examples on usage. Also see
 * {@link OAuth2AccessTokenDetails} for more info on how to use it.
 */
public class OAuth2Client {
    private URL tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private OAuth2AccessTokenDetails currentToken;

    private static OkHttpClient HTTP_CLIENT;

    public OAuth2Client() {
        this.tokenEndpoint = null;
        this.clientId = "";
        this.clientSecret = "";
        this.scope = new HashSet<>();
        this.currentToken = new OAuth2AccessTokenDetails();
    }

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

    public OAuth2AccessTokenDetails getAccessToken() throws TokenException {
        if (currentToken.isExpired()) {
            getNewToken();
        }
        return currentToken;
    }

    public OkHttpClient getHttpClient() {
        if (HTTP_CLIENT == null) {
            // create a client which will supply OAuth client id and secret as HTTP basic authentication
            HTTP_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        }
        return HTTP_CLIENT;
    }

    public static void setHttpClient(OkHttpClient httpClient) {
        // If we had an existing OkHttpClient, it will release its resources automatically
        HTTP_CLIENT = httpClient;
    }

    private void getNewToken() throws TokenException {
        // build the form to post to the token endpoint
        FormBody body = new FormBody.Builder().add("grant_type", "client_credentials")
            .add("scope", String.join(" ", scope)).build();

        // build the POST request to the token endpoint with the form data
        Request request = new Request.Builder()
            .addHeader("Accept", "application/json")
            .url(getTokenEndpoint())
            .post(body)
            .build();

        // We perhaps share our OkHttpClient instance with other OAuth2Client instances, or with an
        // instance gotten from somewhere else through the setHttpClient() method, so we copy the
        // shared instance's configuration and the authorization handler based on our instance
        // variables to it
        // See https://github.com/square/okhttp/wiki/Recipes#per-call-configuration
        OkHttpClient client = getHttpClient().newBuilder()
            .authenticator(new Authenticator() {

            private int retries = 0;
            private int maxRetries = 5;

            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                if (retries >= maxRetries) {
                    return null;
                }
                retries++;
                String credential = Credentials.basic(getClientId(), getClientSecret());
                return response.request().newBuilder()
                    .header("Authorization", credential)
                    .build();
            }
        }).build();

        // make the client execute the POST request
        try (Response response = client.newCall(request).execute()) {
            currentToken = OAuth2AccessTokenDetails.getObject(response);
        }
        catch (IOException e) {
            throw new TokenException(e);
        }
    }
}

