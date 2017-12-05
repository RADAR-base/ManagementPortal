# ManagementPortal

ManagementPortal is an application which is used to manage pilot studies for [RADAR-CNS](http://www.radar-cns.org/).

## Quickstart

The quickest way to get ManagementPortal up and running in production mode is by using the included
docker-compose files. 
1. First, we need to generate a key pair for signing JWT tokens as follows:
```shell
keytool -genkey -alias selfsigned -keyalg RSA -keystore src/main/docker/etc/config/keystore.jks -keysize 4048 -storepass radarbase
```
**Make sure the key password and store password are the same!** This is a requirement for Spring Security.

2. Then, make sure [Docker][] and [Docker-Compose][] are installed on your system.
3. Finally, we can start the stack with `docker-compose -f src/main/docker/management-portal.yml up -d`.

The docker image can be pulled by running `docker pull radarcns/management-portal:0.3.1`.

## Configuration

Management Portal comes with a set of default values for its configuration. These defaults are most
easily overridden by using environment variables.

### Environment Variables

The table below lists the variables that are most likely in need of change when deploying Management
Portal. You can find the complete configuration
in the [application.yml](src/main/resources/config/application.yml) and 
[application-prod.yml](src/main/resources/config/application-prod.yml) files. See
[Spring external configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
for other options on overriding the default configuration.

| Variable                                                   | Default value                                       | Description                                                                                            |
|------------------------------------------------------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`                                    | `jdbc:postgresql://localhost:5432/managementportal` | URL for the database to be used                                                                        |
| `SPRING_DATASOURCE_USERNAME`                               | `<username>`                                        | Username to access the database                                                                        |
| `SPRING_DATASOURCE_PASSWORD`                               | `<password>`                                        | Password to access the database                                                                        |
| `MANAGEMENTPORTAL_FRONTEND_CLIENT_SECRET`                  | None, you need to override this                     | OAuth client secret for the frontend                                                                   |
| `MANAGEMENTPORTAL_FRONTEND_ACCESS_TOKEN_VALIDITY_SECONDS`  | `14400`                                             | Frontend access token validity period in seconds                                                       |
| `MANAGEMENTPORTAL_FRONTEND_REFRESH_TOKEN_VALIDITY_SECONDS` | `259200`                                            | Frontend refresh token validity period in seconds                                                      |
| `MANAGEMENTPORTAL_OAUTH_CLIENTS_FILE`                      | `/mp-includes/config/oauth_client_details.csv`      | Location of the OAuth clients file                                                                     |
| `MANAGEMENTPORTAL_CATALOGUE_SERVER_ENABLE_AUTO_IMPORT`     | `false`                                             | Wether to enable or disable auto import of sources from the catalogue server                           |
| `MANAGEMENTPORTAL_CATALOGUE_SERVER_SERVER_URL`             | None                                                | URL to the catalogue server                                                                            |
| `JHIPSTER_SLEEP`                                           | `10`                                                | Time in seconds that the application should wait at bootup. Used to allow the database to become ready |
| `JAVA_OPTS`                                                | `-Xmx512m`                                          | Options to pass on the JVM                                                                             |

### OAuth Clients

You can add OAuth clients at runtime through the UI, or you can add them to the OAuth clients file
referenced by the `MANAGEMENTPORTAL_OAUTH_CLIENTS_FILE` configuration option.
If your client is supposed to work with the 'Pair app' feature, you need to set a key in it's
`additional_information` map called `dynamic_registration` to `true`. See the aRMT and pRMT
clients for an example. If you want to prevent an OAuth client from being altered through the UI,
you can add a key `protected` and set it to `true` in the `additional_information` map.

## Client libraries

This project provides a Gradle task to generate an [OpenAPI] specification from which client libraries can be automatically generated:
```bash
./gradlew generateOpenApiSpec
```
The resulting file can be imported into the [Swagger editor], or used with [Swagger codegen] to generate client libraries. A Gradle task for generating a Java client is also provided for convenience:
```bash
./gradlew generateJavaClient
```

## Documentation

- [Javadoc](mp-javadoc/)
- [Apidoc](apidoc/)

# RADAR-Auth

## Documentation
- [radar-auth Javadoc](ra-javadoc/)

# OAuth-Client-Util

## Documentation
- [Javadoc](ocu-javadoc/)

# ManagementPortal-Client

## Documentation
- [Javadoc](mpc-javadoc/)

