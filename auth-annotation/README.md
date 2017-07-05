Security Annotation
===================
This project provides an `@Secured` annotation for JAX-RS based REST APIs.

Usage
-----

You can annotate classes or methods with the `@Secured` annotation. A class annotation applies to all methods defined in the class. However method annotations completely override class annotations. They are not additive. E.g. to indicate a method is accessible for tokens that either have the scope `read_thing_1` or `read_thing_2`, and that user must have either the `reader` or `project_owner` role, you would annotate the method with `@Secured(scopesAllowed = {"read_thing_1","read_thing_2"}, rolesAllowed = {"reader", "project_owner"})`

Configuration
-------------

The library expects the identity server configuration in a file called `radar-is.yml`. Either set the environment variable `RADAR_IS_CONFIG_LOCATION` to the full path of the file, or put the file somewhere on the classpath. The file should define the following variables:

| Variable name             | Description                                                                                                                                                                                                                                                                                 |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| username                  | The OAuth client id for accessing the identity server.                                                                                                                                                                                                                                                  |
| password                  | The OAuth client secret for accessing the identity server.                                                                                                                                                                                                                                              |
| tokenValidationEndpoint   | The server's token validation endpoint. The expected response from this endpoint is a JSON structure containing at least the following fields: `aud`, `scope`, `authorities`, `user_name`, `exp` and `jti`                                                                                              |
| publicKeyEndpoint         | Server endpoint that provides the public key of the keypair used to sign the JWTs. The expected response from this endpoint is a JSON structure containing two fields: `alg` and `value`, where `alg` should be equal to `SHA256withRSA`, and `value` should be equal to the public key in PEM format. |

For example:

```yaml
username: test_username
password: test_password
tokenValidationEndpoint: http://localhost:8089/oauth/check_token
publicKeyEndpoint: http://localhost:8089/oauth/token_key
```
