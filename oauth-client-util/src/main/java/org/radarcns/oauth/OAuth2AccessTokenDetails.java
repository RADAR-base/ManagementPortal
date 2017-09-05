package org.radarcns.oauth;

/*
 * Copyright 2017 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import org.radarcns.exception.TokenException;

import java.io.IOException;
import java.time.Instant;

/**
 * This class captures the response from ManagementPortal's /oauth/token endpoint. The actual access
 * token can be retrieved with the {@link #getAccessToken()} method. This is a token in JWT format,
 * and can be parsed with a JWT library of your preference. You can ise {@link #isValid()} to check
 * if you got a valid token, or an error response. If {@link #isValid()} returns <code>false</code>,
 * you can use {@link #getError()} and {@link #getErrorDescription()} to find the error message
 * that was returned by ManagementPortal. You can also use {@link #isExpired()} to find out if the
 * token has expired or not. However it is advised to use the {@link OAuth2Client} class, as it will
 * automatically manage token refreshing and checking validity.
 */
public class OAuth2AccessTokenDetails {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final long expiresIn;

    @JsonProperty("scope")
    private final String scope;

    @JsonProperty("sub")
    private final String subject;

    @JsonProperty("iss")
    private final String issuer;

    @JsonProperty("iat")
    private final long issueDate;

    @JsonProperty("jti")
    private final String jsonWebTokenId;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("error_description")
    private final String errorDescription;

    @JsonProperty("message")
    private final String message;

    /**
     * Constructor.
     * @param accessToken {@link String} representing an Access Token
     * @param tokenType {@link String} defining the Token Type
     * @param expiresIn time in millisecond after which the token will be no longer valid
     * @param scope {@link String} defining the scope for using the token
     * @param subject {@link String} identifying the subject associated to the token
     * @param issuer {@link String} stating the identity that has issued the token
     * @param issueDate time in millisecond when the token has been issued
     * @param jsonWebTokenId {@link String} useful to validate the token
     */
    public OAuth2AccessTokenDetails(String accessToken, String tokenType, long expiresIn, String scope,
        String subject, String issuer, long issueDate, String jsonWebTokenId, String error,
        String errorDescription, String message) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.subject = subject;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.jsonWebTokenId = jsonWebTokenId;
        this.error = error;
        this.errorDescription = errorDescription;
        this.message = message;
    }

    public OAuth2AccessTokenDetails() {
        this.accessToken = null;
        this.tokenType = null;
        this.expiresIn = 0;
        this.scope = null;
        this.subject = null;
        this.issuer = null;
        this.issueDate = 0;
        this.jsonWebTokenId = null;
        this.error = null;
        this.errorDescription = null;
        this.message = null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public String getJsonWebTokenId() {
        return jsonWebTokenId;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        // some errors give error_description field, some give message field
        if (errorDescription != null) {
            return errorDescription;
        }
        else if (message != null) {
            return  message;
        }
        else {
            return "";
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(Instant.ofEpochSecond(issueDate + expiresIn));
    }

    public boolean isValid() {
        return accessToken != null && error == null;
    }

    public static OAuth2AccessTokenDetails getObject(Response response) throws TokenException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String responseBody;
        try {
            responseBody = response.body().string();
        }
        catch (IOException e) {
            throw new TokenException(e);
        }

        try {
            OAuth2AccessTokenDetails result = mapper.readValue(responseBody, OAuth2AccessTokenDetails.class);
            if (result.getError() != null) {
                throw new TokenException(result.getError() + ": " + result.getErrorDescription());
            }
            if (result.getAccessToken() == null) {
                // we didn't catch an error but also didn't get a token (this could happen e.g. when
                // we receive an empty JSON entity as a response, or a JSON entity which does not have
                // the right fields
                throw new TokenException("An unexpected error occured. " + "HTTP status was "
                    + response.code() + ": " + response.message()
                    + ". Response body was: " + responseBody);
            }
            return result;
        }
        catch (Exception e) {
            throw new TokenException(e);
        }
    }
}
