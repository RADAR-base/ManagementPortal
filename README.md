# ManagementPortal

[![Latest release](https://img.shields.io/github/v/release/RADAR-base/ManagementPortal)](https://github.com/RADAR-base/ManagementPortal/releases/latest)
[![Build Status](https://github.com/RADAR-base/ManagementPortal/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/RADAR-base/ManagementPortal/actions/workflows/main.yml?query=branch%3Amaster)

Management Portal is an application which is used to manage clinical studies for [RADAR-base](https://www.radar-base.org/) platform.

## Table of contents

- [Quickstart](#quickstart)
  * [Using Docker-Compose](#using-docker-compose)
  * [Build from source](#build-from-source)
- [Configuration](#configuration)
  * [Environment Variables](#environment-variables)
  * [OAuth Clients](#oauth-clients)
  * [User management by Ory](#user-management)
  * [UI Customization](#ui-customization)
- [Development](#development)
  * [Managing dependencies](#managing-dependencies)
  * [Using angular-cli](#using-angular-cli)
- [Building for production](#building-for-production)
- [Testing](#testing)
  * [Client tests](#client-tests)
  * [Other tests](#other-tests)
- [Using Docker to simplify development (optional)](#using-docker-to-simplify-development-optional)
- [Documentation](#documentation)
- [Client libraries](#client-libraries)

## Dependencies

The following are the prerequisites to run ManagementPortal from source on your machine:

- Java 17
- Node.js (v16 or later is recommended)
- Yarn (3.1.0 or later is recommended)

## Quickstart

Management Portal can be easily run either by running from source or by using the provided `docker-compose` file.  For documentation on how to run ManagementPortal in production, please see [RADAR-Kubernetes][].

### Using Docker-Compose

The quickest way to get Management Portal up and running in production mode is by using the included
docker-compose files. 
1. Make sure [Docker][] and [Docker-Compose][] are installed on your system.
2. Generate a key pair for signing JWT tokens as follows:
   ```shell
   keytool -genkeypair -alias radarbase-managementportal-ec -keyalg EC -validity 3650 -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore src/main/docker/etc/config/keystore.p12 -storepass radarbase -keypass radarbase
   ```
3. Now, we can start the stack with `docker-compose -f src/main/docker/management-portal.yml up -d`.

This will start a Postgres database, ManagementPortal and the [kratos identity provider stack](https://www.ory.sh/docs/kratos/ory-kratos-intro). The default password for the `admin`
account is `admin`. An angular live development server to access the managementportal can be started using the `yarn start` command (see [Development](#development)).

### Build from source

You must install and configure the following dependencies on your machine to run from source.
1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.
2. [Yarn][]: We use Yarn to manage Node dependencies.
   Depending on your system, you can install Yarn either from source or as a pre-packaged bundle.
3. Generate a key pair for signing JWT tokens as follows:
   ```shell
   keytool -genkeypair -alias radarbase-managementportal-ec -keyalg EC -validity 3650 -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore src/main/resources/config/keystore.p12 -storepass radarbase -keypass radarbase
   ```
   **Make sure the key password and store password are the same!** This is a requirement for Spring Security.

4. **Profile configurations :** ManagementPortal can be run with either `development` or `production` profile. The table below lists the
main differences between the profiles. Configure the application using the property file at `src/main/resources/config/application-<profile>.yml`.Read more about configurations [here](#configuration)
    
5. Run ManagementPortal by running `./gradlew bootRun -Pprod` or `./gradlew bootRun -Pdev`. Development mode will start an in
memory database and ManagementPortal. An angular live development server to access the managementportal can be started using the `yarn start` command (see [Development](#development)).
6. You can log in to the application using `admin:admin`. Please don't forgot to change the password of `admin`, if you are using the application on production environment.
7. The identity server stack can be started in docker by using the docker compose command `docker-compose -f .\src\main\docker\app.yml up -d kratos kratos-selfservice-ui-node kratos-migrate postgresd-kratos mailslurper`


|                                  | Development     | Production                        |
|----------------------------------|-----------------|-----------------------------------|
| Database type                    | In-memory       | Postgres                          |
| Demo data loaded                 | Yes             | No                                |




The docker image can be pulled by running `docker pull radarbase/management-portal:latest`.

## Configuration

Management Portal comes with a set of default values for its configuration. You can either modify
the `application.yml` and `application-prod.yml` (or `application-dev.yml` when running the
development profile) before building the application, or override the defaults using environment
variables.

### Environment Variables

The table below lists the variables that are most likely in need of change when deploying Management
Portal. You can find the complete configuration
in the [application.yml](src/main/resources/config/application.yml) and 
[application-prod.yml](src/main/resources/config/application-prod.yml) files. See
[Spring external configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
for other options on overriding the default configuration.

| Variable                                                    | Default value                                       | Description                                                                                                                                                                                                                                 |
|-------------------------------------------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`                                     | `jdbc:postgresql://localhost:5432/managementportal` | URL for the database to be used                                                                                                                                                                                                             |
| `SPRING_DATASOURCE_USERNAME`                                | `<username>`                                        | Username to access the database                                                                                                                                                                                                             |
| `SPRING_DATASOURCE_PASSWORD`                                | `<password>`                                        | Password to access the database                                                                                                                                                                                                             |
| `SPRING_APPLICATION_JSON`                                   | None                                                | Generic environment variable for overriding all types of application settings                                                                                                                                                               |
| `MANAGEMENTPORTAL_MAIL_FROM`                                | None, you need to override this                     | Email address that will be set  in the From email header.                                                                                                                                                                                   |
| `MANAGEMENTPORTAL_FRONTEND_CLIENT_SECRET`                   | None, you need to override this                     | OAuth client secret for the frontend                                                                                                                                                                                                        |
| `MANAGEMENTPORTAL_FRONTEND_ACCESS_TOKEN_VALIDITY_SECONDS`   | `14400`                                             | Frontend access token validity period in seconds                                                                                                                                                                                            |
| `MANAGEMENTPORTAL_FRONTEND_REFRESH_TOKEN_VALIDITY_SECONDS`  | `259200`                                            | Frontend refresh token validity period in seconds                                                                                                                                                                                           |
| `MANAGEMENTPORTAL_OAUTH_CLIENTS_FILE`           ****        | `/mp-includes/config/oauth_client_details.csv`      | Location of the OAuth clients file                                                                                                                                                                                                          |
| `MANAGEMENTPORTAL_OAUTH_KEY_STORE_PASSWORD`                 | `radarbase`                                         | Password for the JWT keystore                                                                                                                                                                                                               |
| `MANAGEMENTPORTAL_OAUTH_SIGNING_KEY_ALIAS`                  | `radarbase-managementportal-ec`                     | Alias in the keystore of the keypair to use for signing                                                                                                                                                                                     |
| `MANAGEMENTPORTAL_OAUTH_ENABLE_PUBLIC_KEY_VERIFIERS`        | `false`                                             | Whether to use additional verifiers using public-keys and deprecated verifier implementation. If you set this to `true`, also set `RADAR_IS_CONFIG_LOCATION` and provide yaml file with public keys. Read more at radar-auth documentation. |
| `MANAGEMENTPORTAL_CATALOGUE_SERVER_ENABLE_AUTO_IMPORT`      | `false`                                             | Whether to enable or disable auto import of sources from the catalogue server                                                                                                                                                               |
| `MANAGEMENTPORTAL_CATALOGUE_SERVER_SERVER_URL`              | None                                                | URL to the catalogue server                                                                                                                                                                                                                 |
| `MANAGEMENTPORTAL_COMMON_BASE_URL`                          | None                                                | Resolvable baseUrl of the hosted platform                                                                                                                                                                                                   |
| `MANAGEMENTPORTAL_COMMON_MANAGEMENT_PORTAL_BASE_URL`        | None                                                | Resolvable baseUrl of this managementportal  instance                                                                                                                                                                                       |
| `MANAGEMENTPORTAL_COMMON_PRIVACY_POLICY_URL`                | None                                                | Resolvable URL to the common privacy policy url                                                                                                                                                                                             |
| `MANAGEMENTPORTAL_COMMON_ADMIN_PASSWORD`                    | None                                                | Admin password                                                                                                                                                                                                                              |
| `MANAGEMENTPORTAL_COMMON_ACTIVATION_KEY_TIMEOUT_IN_SECONDS` | 86400                                               | Account activation/reset timeout in seconds                                                                                                                                                                                                 |
| `RADAR_IS_CONFIG_LOCATION`                                  | `radar-is.yml` from class path                      | Location of additional public-key configuration file.                                                                                                                                                                                       |
| `JHIPSTER_SLEEP`                                            | `10`                                                | Time in seconds that the application should wait at bootup. Used to allow the database to become ready                                                                                                                                      |
| `JAVA_OPTS`                                                 | `-Xmx512m`                                          | Options to pass on the JVM                                                                                                                                                                                                                  |

Lists cannot directly be encoded by environment variables in this version of Spring. So for example the OAuth checking key aliases need to be encoded using the `SPRING_APPLICATION_JSON` variable. For setting two aliases, set it to `{"managementportal":{"oauth":{"checkingKeyAliases":["one","two"]}}}`, for example. If this list is not set, the signing key will also be used as the checking key.

### OAuth Clients

ManagementPortal uses `OAuth2` workflow to provide authentication and authorization. To add new OAuth clients, you can add at runtime through the UI, or you can add them to the OAuth clients file
referenced by the `MANAGEMENTPORTAL_OAUTH_CLIENTS_FILE` configuration option.
- If your client is supposed to work with the `Pair app` feature, you need to set a key called `dynamic_registration` to `true` like this `{"dynamic_registration": true}` in its `additional_information` map. See the aRMT and pRMT
clients for an example. 
- If your client is `dynamic_registration` enabled, the QR code generated by `Pair app` feature will contain a short-living URL. By doing a `GET` request on that URL the `refresh-token` and related meta-data can be fetched. 
- If you want to prevent an OAuth client from being altered through the UI, you can add a key `{"protected": true}` in the `additional_information` map. 

If the app is paired via the Pair App dialog, the QR code that will be scanned contains a short-lived URL, e.g. `https://radar-base-url.org/api/meta-token/bMUkowOmTOci`

Your app should access the URL, where it will receive an OAuth2 
refresh token as well as the platform's base URL and a URL to the privacy policy. No authorization 
is required to access this URL. **Important:** For security reasons, the information at this URL can
only be accessed once. Once it has been accessed it can not be retrieved again. 

The app can use that refresh token to get new access and refresh tokens by doing the following HTTP 
request to the base URL, using HTTP basic authentication with your OAuth client ID as username, and 
an empty password.
```
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&refresh_token=<refresh_token>
```
This will respond with at least the access token and refresh token:
```json
{
   "access_token": "...",
   "refresh_token": "...",
   "expires_in": 14400
}
```
Both tokens are valid for a limited time only. When the access token runs out, you will need to 
perform another request like the one above, but you need to use the new `refresh_token`, since 
refresh tokens are valid only once.

### Authorization Code flow
The code grant flow for OAuth2 clients can be the following:
1. Register an oauth-client with grant_type `authorization_code` and add a valid `redirect_uri` to that client. (`e.g. https://my.example.com/oauth_redirect` in this example)
2. Ask user authorization for your app:
     ```
     GET /oauth/authorize?client_id=MyId&response_type=code&redirect_uri=https://my.example.com/oauth_redirect
     ```
   where you replace `MyId` with your OAuth client id. This needs to be done from a interactive
   web view, either a browser or a web window. If the user approves, this will redirect to
   `https://my.example.com/oauth_redirect?code=abcdef`. In Android, with [https://appauth.io]
   (AppAuth library), the URL could be `com.example.my://oauth_redirect` for the `com.example.my`
   app.
   You can add an optional parameter for `state`. If you add the state parameter, it will be returned with the `code`.
3. Request a token for your app by doing a POST, again with HTTP basic authentication with as
   username your OAuth client id, and leaving the password empty:
    ```
    POST /oauth/token
    Content-Type: application/x-www-form-urlencoded

    grant_type=authorization_code&code=abcdef&redirect_uri=https://my.example.com/oauth_redirect
    ```
   This will respond with the access token and refresh token:
    ```json
    {
       "access_token": "...",
       "refresh_token": "..."
    }
    ```
   Now the app can use the refresh token flow as shown above.

### Client credentials flow
The code grant flow for OAuth2 clients can also be the following:
1. Register an oauth-client with grant_type `client_credentials`
2. Request a token for your app by doing a POST with HTTP basic authentication with as
   username your OAuth client id and password your OAuth client secret:
    ```
    POST /oauth/token
    Content-Type: application/x-www-form-urlencoded

    grant_type=client_credentials
    ```
   This will respond with the access token:
    ```json
    {
        "access_token": "...",
        "token_type": "bearer",
      
        "...": "..."
    }
    ```
   Now the app can use the access token flow.

### User management
Organizational user management and authorization for the managementportal is performed by [Ory Kratos](https://www.ory.sh/docs/kratos/ory-kratos-intro). The flow for adding users to the portal is as follows:

1. Navigate to the [User management view](http://127.0.0.1:8081/#/user-management) and create a user.
2. The new user can then [verify](http://127.0.0.1:3000/verification) their email, [reset their password](http://127.0.0.1:3000/recovery) and [add two-factor authentication](http://127.0.0.1:3000/settings?#totp) at the kratos self-service node
3. Use these credentials to log in to managementportal.

```mermaid
sequenceDiagram
    participant radar as Kratos self-service node
    actor colleague as User
    actor researcher as Admin
    participant mp as ManagementPortal
    participant kratos as Kratos


    #== User Registration ==
    colleague -->> researcher: Request account (email required)
    researcher -->> mp: Create user
    mp -->> kratos: Create kratos identity
    colleague -->> radar: Verify email
    radar -->> kratos:  
    colleague -->> radar: Reset password
    radar -->> kratos: 
    colleague -->> radar: Activate 2-FA
    radar -->> kratos: 
    colleague -->> mp: Login (2-FA required)
```

### UI Customization

You can customize ManagementPortal web app by replacing images located in `src/main/webapp/content/images` with your logos:
- `navbar-logo.png` is a 70x45 (WxH in pixels) image shown at the top of every page;
- `home-page-logo.png` is shown on the home page only; 350x350 px image recommended.

Once you build the project, you will find these images in `build/www/assets/images`.

## Development

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.
2. [Yarn][]: We use Yarn to manage Node dependencies.
   Depending on your system, you can install Yarn either from source or as a pre-packaged bundle.
3. Local SMTP server: currently a simple docker-compose is provided with a local SMTP server. Create `smtp.env` from `smtp.env.template` and modify `application.yml` accordingly.  

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

    yarn install

We use yarn scripts and [Webpack][] as our build system.


Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./gradlew
    yarn start

Then open <http://localhost:8081/> to start the interface and sign in with admin/admin.

### Managing dependencies

[Yarn][] is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all the scripts available to run for this project.

### Using angular-cli

You can also use [Angular CLI][] to generate some custom client code.

For example, the following command:

    ng generate component my-component

will generate few files:

    create src/main/webapp/app/my-component/my-component.component.html
    create src/main/webapp/app/my-component/my-component.component.ts
    update src/main/webapp/app/app.module.ts

## On Production
### Building for production

To optimize the ManagementPortal application for production, run:

    ./gradlew -Pprod clean bootWar
### Hosting in production
The latest Meta-QR code implementation requires REST resources on `api/meta-token/*` should definitely be rate-limited by upstream servers.

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar build/libs/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.


## Testing

To launch your application's tests, run:

    ./gradlew test

### Client tests

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in `src/test/javascript/` and can be run with:

    yarn test

UI end-to-end tests are powered by [Cypress][], which is built on top of WebDriverJS. They're located in [src/test/javascript/e2e](src/test/javascript/e2e)
and can be run by starting Spring Boot in one terminal (`./gradlew bootRun`) and running the tests (`yarn run e2e`) in a second one.
### Other tests

Performance tests are run by [Gatling][] and written in Scala. They're located in `src/test/gatling` and can be run with:

    ./gradlew gatlingRunAll

or

    ./gradlew gatlingRun<SIMULATION_CLASS_NAME> # E.g., gatlingRunProjectGatlingTest

For more information, refer to the [Running tests page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.
For example, to start a postgreSQL database in a docker container, run:

    docker-compose -f src/main/docker/postgresql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/postgresql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./gradlew bootWar -Pprod buildDocker

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`yo jhipster:docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Documentation

Please find the links for some of the documentation per category/component
* [management-portal-javadoc](https://radar-base.github.io/ManagementPortal/management-portal-javadoc/)
* [radar-auth-javadoc](https://radar-base.github.io/ManagementPortal/radar-auth-javadoc/)
* [managementportal-client-javadoc](https://radar-base.github.io/ManagementPortal/managementportal-client-javadoc/)
* [Swagger 2.0 apidoc](https://radar-base.github.io/ManagementPortal/apidoc/swagger.json)

The pages site is published from the `gh-pages` branch, which has its own history. If you want to
contribute to the documentation, it is probably more convenient to clone a separate copy of this
repository for working on the `gh-pages` branch:
```bash
git clone --branch gh-pages https://github.com/RADAR-base/ManagementPortal.git ManagementPortal-docs
```
## Client libraries

This project provides a Gradle task to generate an [OpenAPI] specification from which client libraries can be automatically generated:
```bash
./gradlew generateOpenApiSpec
```
ManagementPortal needs to be running and be accessible at `http://localhost:8080` for this task to work.

The resulting file can be imported into the [Swagger editor][], or used with [Swagger codegen][] to generate client libraries.

[JHipster Homepage and latest documentation]: https://www.jhipster.tech

[RADAR-Kubernetes]: https://github.com/RADAR-base/RADAR-Kubernetes
[Using Docker and Docker-Compose]: https://www.jhipster.tech/docker-compose/
[Running tests page]: https://www.jhipster.tech/running-tests/
[Setting up Continuous Integration]: https://www.jhipster.tech/setting-up-ci/

[Gatling]: https://gatling.io/
[Node.js]: https://nodejs.org/
[Yarn]: https://yarnpkg.org/
[Webpack]: https://webpack.github.io/
[Angular CLI]: https://cli.angular.io/
[BrowserSync]: http://www.browsersync.io/
[Karma]: https://karma-runner.github.io/
[Jasmine]: https://jasmine.github.io
[Cypress]: https://www.cypress.io
[Protractor]: https://angular.github.io/protractor/
[Leaflet]: http://leafletjs.com/
[DefinitelyTyped]: http://definitelytyped.org/
[Docker]: https://docs.docker.com/
[Docker-Compose]: https://docs.docker.com/compose/
[OpenAPI]: https://www.openapis.org/
[Swagger editor]: https://editor.swagger.io/
[Swagger codegen]: https://swagger.io/swagger-codegen/
[OAuth2 spec]: https://tools.ietf.org/html/rfc6749#section-9
