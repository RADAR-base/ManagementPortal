# ManagementPortal

ManagementPortal is an application which is used to manage pilot studies for [RADAR-CNS](http://www.radar-cns.org/).

## Quickstart

The quickest way to get ManagementPortal up and running in production mode is by using the included
docker-compose files. 
1. First, we need to generate a key pair for signing JWT tokens: `keytool -genkey -alias selfsigned -keyalg RSA -keystore src/main/docker/etc/config/keystore.jks -keysize 4048 -storepass radarbase`

**Make sure the key password and store password are the same!** This is a requirement for Spring Security.

2. Then, make sure [Docker][] and [Docker-Compose][] are installed on your system.
3. Finally, we can start the stack with `docker-compose -f src/main/docker/management-portal.yml up -d`.

For more details on configuring Management Portal we refer to the readme on our [Github repository].

- [Javadoc](mp-javadoc/)
- [Apidoc](apidoc/)

# RADAR-Auth

RADAR authentication and authorization library. This project provides classes for authentication and
authorization of clients connecting to components of the RADAR platform.

- [Github](https://github.com/RADAR-CNS/ManagementPortal/tree/master/radar-auth/)
- [Bintray](https://bintray.com/radar-cns/org.radarcns/radar-auth)
- [Javadoc](ra-javadoc/)

# OAuth-Client-Util

This library can be used by client applications that want to use the `client_credentials` OAuth2 
flow. It will manage getting the token and renewing it when necessary.

- [Github](https://github.com/RADAR-CNS/ManagementPortal/tree/master/oauth-client-util/)
- [Bintray](https://bintray.com/radar-cns/org.radarcns/oauth-client-util)
- [Javadoc](ocu-javadoc/)

# ManagementPortal-Client

Client library for accessing Management Portal from a Java application. This library is auto-generated using swagger-codegen.

- [Bintray](https://bintray.com/radar-cns/org.radarcns/managementportal-client)
- [Javadoc](mpc-javadoc/)


[Docker]: https://docs.docker.com/
[Docker-Compose]: https://docs.docker.com/compose/
[OpenAPI]: https://www.openapis.org/
[Swagger editor]: http://editor.swagger.io/
[Swagger codegen]: https://swagger.io/swagger-codegen/
[Github repository]: https://github.com/RADAR-CNS/ManagementPortal/
