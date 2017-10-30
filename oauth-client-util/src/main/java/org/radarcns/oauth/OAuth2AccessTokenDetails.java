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
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("sub")
    private String subject;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("iat")
    private long issueDate;

    @JsonProperty("jti")
    private String jsonWebTokenId;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("message")
    private String message;

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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setIssueDate(long issueDate) {
        this.issueDate = issueDate;
    }

    public void setJsonWebTokenId(String jsonWebTokenId) {
        this.jsonWebTokenId = jsonWebTokenId;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public void setMessage(String message) {
        this.message = message;
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
