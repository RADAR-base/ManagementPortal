# ManagementPortal

[![Build Status](https://travis-ci.org/RADAR-CNS/ManagementPortal.svg?branch=master)](https://travis-ci.org/RADAR-CNS/ManagementPortal)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/87bb961266d3443988b52ee7aa32f100)](https://www.codacy.com/app/RADAR-CNS/ManagementPortal?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RADAR-CNS/ManagementPortal&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/87bb961266d3443988b52ee7aa32f100)](https://www.codacy.com/app/RADAR-CNS/ManagementPortal?utm_source=github.com&utm_medium=referral&utm_content=RADAR-CNS/ManagementPortal&utm_campaign=Badge_Coverage)

ManagementPortal is an application which is used to manage pilot studies for [RADAR-CNS](http://www.radar-cns.org/).

## Table of contents

- [Quickstart](#quickstart)
- [Configuration](#configuration)
  * [Environment Variables](#environment-variables)
  * [OAuth Clients](#oauth-clients)
- [Documentation](#documentation)
- [Development](#development)
  * [Managing dependencies](#managing-dependencies)
  * [Using angular-cli](#using-angular-cli)
- [Building for production](#building-for-production)
- [Testing](#testing)
  * [Client tests](#client-tests)
  * [Other tests](#other-tests)
- [Using Docker to simplify development (optional)](#using-docker-to-simplify-development--optional-)
- [Continuous Integration (optional)](#continuous-integration--optional-)
- [Client libraries](#client-libraries)


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

## Documentation

Visit our [Github pages](https://radar-cns.github.io/ManagementPortal) site to find links to the
Javadoc and API docs.

The pages site is published from the `gh-pages` branch, which has its own history. If you want to
contribute to the documentation, it is probably more convenient to clone a separate copy of this
repository for working on the `gh-pages` branch:
```bash
git clone --branch gh-pages https://github.com/RADAR-CNS/ManagementPortal.git ManagementPortal-docs
```

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

Then open <http://localhost:8080/> to start the interface and sign in with admin/admin.

[Yarn][] is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all of the scripts available to run for this project.

### Managing dependencies

For example, to add [Leaflet][] library as a runtime dependency of your application, you would run following command:

    yarn add --exact leaflet

To benefit from TypeScript type definitions from [DefinitelyTyped][] repository in development, you would run following command:

    yarn add --dev --exact @types/leaflet

Then you would import the JS and CSS files specified in library's installation instructions so that [Webpack][] knows about them:

Edit [src/main/webapp/app/vendor.ts](src/main/webapp/app/vendor.ts) file:
~~~
import 'leaflet/dist/leaflet.js';
~~~

Edit [src/main/webapp/content/css/vendor.css](src/main/webapp/content/css/vendor.css) file:
~~~
@import '~leaflet/dist/leaflet.css';
~~~

Note: there are still few other things remaining to do for Leaflet that we won't detail here.

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

### Using angular-cli

You can also use [Angular CLI][] to generate some custom client code.

For example, the following command:

    ng generate component my-component

will generate few files:

    create src/main/webapp/app/my-component/my-component.component.html
    create src/main/webapp/app/my-component/my-component.component.ts
    update src/main/webapp/app/app.module.ts

## Building for production

To optimize the ManagementPortal application for production, run:

    ./gradlew -Pprod clean bootRepackage

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar build/libs/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

## Testing

To launch your application's tests, run:

    ./gradlew test

### Client tests

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in [src/test/javascript/](src/test/javascript/) and can be run with:

    yarn test

UI end-to-end tests are powered by [Protractor][], which is built on top of WebDriverJS. They're located in [src/test/javascript/e2e](src/test/javascript/e2e)
and can be run by starting Spring Boot in one terminal (`./gradlew bootRun`) and running the tests (`yarn run e2e`) in a second one.
### Other tests

Performance tests are run by [Gatling][] and written in Scala. They're located in [src/test/gatling](src/test/gatling) and can be run with:

    ./gradlew gatlingRun

For more information, refer to the [Running tests page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.
For example, to start a postgreSQL database in a docker container, run:

    docker-compose -f src/main/docker/postgresql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/postgresql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./gradlew bootRepackage -Pprod buildDocker

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`yo jhipster:docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`yo jhipster:ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

## Client libraries

This project provides a Gradle task to generate an [OpenAPI] specification from which client libraries can be automatically generated:
```bash
./gradlew generateOpenApiSpec
```
The resulting file can be imported into the [Swagger editor], or used with [Swagger codegen] to generate client libraries. A Gradle task for generating a Java client is also provided for convenience:
```bash
./gradlew generateJavaClient
```

[JHipster Homepage and latest documentation]: https://jhipster.github.io
[JHipster 4.3.0 archive]: https://jhipster.github.io/documentation-archive/v4.3.0

[Using JHipster in development]: https://jhipster.github.io/documentation-archive/v4.3.0/development/
[Using Docker and Docker-Compose]: https://jhipster.github.io/documentation-archive/v4.3.0/docker-compose
[Using JHipster in production]: https://jhipster.github.io/documentation-archive/v4.3.0/production/
[Running tests page]: https://jhipster.github.io/documentation-archive/v4.3.0/running-tests/
[Setting up Continuous Integration]: https://jhipster.github.io/documentation-archive/v4.3.0/setting-up-ci/

[Gatling]: http://gatling.io/
[Node.js]: https://nodejs.org/
[Yarn]: https://yarnpkg.org/
[Webpack]: https://webpack.github.io/
[Angular CLI]: https://cli.angular.io/
[BrowserSync]: http://www.browsersync.io/
[Karma]: http://karma-runner.github.io/
[Jasmine]: http://jasmine.github.io/2.0/introduction.html
[Protractor]: https://angular.github.io/protractor/
[Leaflet]: http://leafletjs.com/
[DefinitelyTyped]: http://definitelytyped.org/
[Docker]: https://docs.docker.com/
[Docker-Compose]: https://docs.docker.com/compose/
[OpenAPI]: https://www.openapis.org/
[Swagger editor]: http://editor.swagger.io/
[Swagger codegen]: https://swagger.io/swagger-codegen/
