package org.radarcns.oauth;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Class for handling OAuth2 client credentials grant
 */
public class OAuth2Client {
    private String managementPortalUrl;
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private OAuth2AccessToken currentToken;

    private static OkHttpClient HTTP_CLIENT;

    public OAuth2Client() {
        this.managementPortalUrl = "";
        this.clientId = "";
        this.clientSecret = "";
        this.scope = new HashSet<>();
        this.currentToken = new OAuth2AccessToken();
    }

    public String getManagementPortalUrl() {
        return managementPortalUrl;
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

    public OAuth2Client managementPortalUrl(String managementPortalUrl) {
        this.managementPortalUrl = managementPortalUrl;
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

    public OAuth2AccessToken getAccessToken() {
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
        }
        return HTTP_CLIENT;
    }

    private void getNewToken() {
        // build the form to post to the token endpoint
        FormBody body = new FormBody.Builder().add("grant_type", "client_credentials")
            .add("scope", String.join(" ", scope)).build();

        // build the POST request to the token endpoint with the form data
        Request request = new Request.Builder()
            .addHeader("Accept", "application/json")
            .url(getManagementPortalUrl() + "/oauth/token")
            .post(body)
            .build();

        // make the client execute the POST request
        try {
            Response response = getHttpClient().newCall(request).execute();
            currentToken = OAuth2AccessToken.getObject(response);
        }
        catch (IOException e) {
            currentToken = new  OAuth2AccessToken(null, null, 0, null, null, null, 0, null,
                "io_error", e.getMessage(), null);
        }
    }
}

