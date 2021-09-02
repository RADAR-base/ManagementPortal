# Build stage
FROM openjdk:11-jdk as builder

# install node
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get install -yq nodejs build-essential && \
    npm install -g npm && \
    npm install -g yarn

# installing the node packages before adding the src directory will allow us to re-use these image layers when only the souce code changes
WORKDIR /app

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.project.prod=true"

COPY package.json postcss.config.js proxy.conf.json tsconfig-aot.json tsconfig.json tslint.json yarn.lock /app/
COPY webpack webpack
RUN yarn install

COPY gradlew /app/
COPY gradle/wrapper gradle/wrapper
RUN ./gradlew --version

COPY gradle gradle
COPY build.gradle gradle.properties settings.gradle /app/
COPY radar-auth/build.gradle radar-auth/
COPY radar-auth/deprecated-auth0/build.gradle radar-auth/deprecated-auth0/
COPY oauth-client-util/build.gradle oauth-client-util/

RUN ./gradlew downloadDependencies :radar-auth:shadowJar

# now we copy our application source code and build it
COPY radar-auth radar-auth
COPY src src
RUN ./gradlew -s bootWar

# Run stage
FROM openjdk:11-jre-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0

RUN apt-get update && apt-get install -y --no-install-recommends \
  curl \
  && rm -rf /var/lib/apt/lists/*

# Add the war and changelogs files from build stage
COPY --from=builder /app/build/libs/*.war /app.war
COPY --from=builder /app/src/main/docker/etc /mp-includes

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
