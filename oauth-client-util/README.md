OAuth2.0 Client utility library
===============================
This library can be used by client applications that want to use the `client_credentials` OAuth2 
flow. It will manage getting the token and renewing it when necessary.

Usage
-----

Quickstart:

```groovy
dependencies {
  compile group: 'org.radarcns', name: 'oauth-client-util', version: '0.1'
}
```

Initializing the client:
```Java
OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read")
            .addScope("write");
```
Getting a token:
```Java
try {
    OAuth2AccessToken token = client.getAccessToken();
}
catch (TokenException e) {
    // handle error
}
```
Checking expiry:
```Java
if (token.isExpired()) {
    // get a new token
    try {
        token = client.getAccessToken();
    }
    catch (TokenException e) {
        // handle error
    }
}
```
Using the token:
```Java
String authorizationHeader = "Authorization: " + token.getTokenType() + " " token.getAccessToken();
```


Create an `OAuth2Client` object and give it the necessary parameters like this:

```Java
OAuth2Client client = new OAuth2Client()
            .clientId("client")
            .clientSecret("secret")
            .managementPortalUrl("http://localhost:8089")
            .addScope("read")
            .addScope("write");
```

Now all you have to do is use the client's `getAccessToken()` method: 
```Java
OAuth2AccessToken token = client.getAccessToken();
```

The client will automatically get a new access token if the current one is expired. If it is not
expired, no new access token will be requested from the server. The actual token string is
accessible through `OAuth2AccessToken.getAccessToken()`.

If there was an issue retrieving the access token, `token.isValid()` will return `false` and you can
check `token.getError()` and `token.getErrorDescription()` to find out more. Note that a valid
token that got expired is still a considered a valid token and will return true on 
`token.isValid()`. To check expiry you should call `token.isExpired`.
