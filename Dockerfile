# Build stage
FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk as builder

# Install NodeJS and Yarn
RUN curl -sL https://deb.nodesource.com/setup_16.x | bash \
     && apt-get update && apt-get install -y \
        nodejs \
     && rm -rf /var/lib/apt/lists/* \
     && npm install -g yarn

## installing the node and java packages before adding the src directory
## will allow us to re-use these image layers when only the souce code changes
WORKDIR /code

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.project.prod=true -Dorg.gradle.vfs.watch=false"
ARG BASE_URL
ARG KRATOS_URL

COPY package.json yarn.lock .yarnrc.yml /code/
COPY .yarn /code/.yarn
RUN yarn install --network-timeout 1000000

COPY gradle gradle
COPY gradlew build.gradle gradle.properties settings.gradle /code/
COPY radar-auth/build.gradle radar-auth/

RUN ./gradlew downloadDependencies

# now we copy our application source code and build it

COPY angular.json proxy.conf.json tsconfig.app.json \
    tsconfig.spec.json tsconfig.json tslint.json /code/
COPY webpack webpack

COPY radar-auth radar-auth
COPY src src
RUN ./gradlew -s bootWar

# Run stage
FROM eclipse-temurin:17-jre

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="" 

# Add the war and changelogs files from build stage
COPY --from=builder /code/build/libs/*.war /app.war
COPY --from=builder /code/src/main/docker/etc /mp-includes

EXPOSE 8080 5701/udp
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java $JAVA_OPTS \
        -Djava.security.egd=file:/dev/./urandom \
        --add-modules java.se \
        --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        --add-opens java.base/java.nio=ALL-UNNAMED \
        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
        --add-opens java.management/sun.management=ALL-UNNAMED \
        --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
        -cp /mp-includes:/app.war \
        org.springframework.boot.loader.WarLauncher
