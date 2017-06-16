Security Annotation
===================
This project provides an `@Secured` annotation for JAX-RS based REST APIs.

Usage
-----

You can annotate classes or methods with the `@Secured` annotation. A class annotation applies to all methods defined in the class. However method annotations completely override class annotations. They are not additive. E.g. to indicate a method is accessible for tokens that either have the scope `read_thing_1` or `read_thing_2`, and that user must have either the `reader` or `project_owner` role, you would annotate the method with `@Secured(scopesAllowed = {"read_thing_1","read_thing_2"}, rolesAllowed = {"reader", "project_owner"})`

### Environment Variables

The following environment variables are used to configure this library:

| Environment variable name   | Description                                                                                                                                               | Required |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `RADAR_IS_URL`              | The URL to the identity server.                                                                                                                           | Yes      |
| `RADAR_IS_CLIENT_ID`        | The OAuth client id for accessing the identity server.                                                                                                    | Yes      |
| `RADAR_IS_CLIENT_SECRET`    | The OAuth client secret for accessing the identity server.                                                                                                | Yes      |
| `RADAR_IS_SIGNING_KEY`      | Public key of the RSA keypair the identity server uses to sign tokens. If this is not defined, it will be fetched from `${RADAR_IS_URL}/oauth/token_key`. | No       |


