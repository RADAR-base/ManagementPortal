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

package org.radarbase.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.radarbase.exception.TokenException;

import java.time.Instant;

/**
 * This class captures the response from ManagementPortal's /oauth/token endpoint. The actual access
 * token can be retrieved with the {@link #getAccessToken()} method. This is a token in JWT format,
 * and can be parsed with a JWT library of your preference. You can use {@link #isValid()} to check
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

    /**
     * Get the error description.
     *
     * <p>Some errors cause an error description to be populated in the {@code
     * error_description} field, other cause the {@code message} field to be populated. This
     * method first checks for the {@code error_description} field, and returns it if not null.
     * Otherwise it checks the {@code message} field and returns that if not null. If both
     * fields are null this method returns an empty string.</p>
     * @return the error description
     */
    public String getErrorDescription() {
        // some errors give error_description field, some give message field
        if (errorDescription != null) {
            return errorDescription;
        } else if (message != null) {
            return  message;
        } else {
            return "";
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(getExpiryDate());
    }

    /**
     * Check the validity of this token.
     * @return {@code true} if the {@code accessToken} field is not {@code null} and the {@code
     *     error} field is null
     */
    public boolean isValid() {
        return accessToken != null && error == null;
    }

    public Instant getExpiryDate() {
        return Instant.ofEpochSecond(issueDate + expiresIn);
    }

    /**
     * Parse an access token response into an {@link OAuth2AccessTokenDetails} object.
     * @param responseBody the response body
     * @return an instance of this class
     * @throws TokenException if the response can not be parsed to an instance of this class
     */
    public static OAuth2AccessTokenDetails getObject(String responseBody) throws TokenException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            OAuth2AccessTokenDetails result = mapper.readValue(responseBody,
                    OAuth2AccessTokenDetails.class);
            if (result.getError() != null) {
                throw new TokenException(result.getError() + ": " + result.getErrorDescription());
            }
            if (result.getAccessToken() == null) {
                // we didn't catch an error but also didn't get a token (this could happen e.g. when
                // we receive an empty JSON entity as a response, or a JSON entity which does
                // not have the right fields
                throw new TokenException("An unexpected error occured."
                    + " Response body was: " + responseBody);
            }
            return result;
        } catch (Exception e) {
            throw new TokenException(e);
        }
    }
}
