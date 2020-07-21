# Build stage
FROM openjdk:8-jdk as builder

# install node
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get install -yq nodejs build-essential && \
    npm install -g npm yarn

# installing the node packages before adding the src directory will allow us to re-use these image layers when only the souce code changes
WORKDIR /app
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.project.prod=true"
COPY gradlew /app/
COPY gradle/wrapper gradle/wrapper
RUN ./gradlew --version

COPY gradle gradle
COPY build.gradle gradle.properties settings.gradle /app/
COPY radar-auth/build.gradle radar-auth/
COPY radar-auth/deprecated-auth0/build.gradle radar-auth/deprecated-auth0
COPY oauth-client-util/build.gradle oauth-client-util/
COPY radar-auth radar-auth
RUN ./gradlew radar-auth:shadowJar

RUN ./gradlew downloadDependencies

COPY package.json postcss.config.js proxy.conf.json tsconfig-aot.json tsconfig.json tslint.json yarn.lock /app/
COPY webpack webpack
RUN ./gradlew -s yarn_install

# now we copy our application source code and build it
COPY radar-auth radar-auth
COPY src src
RUN ./gradlew -s bootRepackage

# Run stage
FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0

# Add the war and changelogs files from build stage
COPY --from=builder /app/build/libs/*.war /app.war
COPY --from=builder /app/src/main/docker/etc /mp-includes

EXPOSE 8080 5701/udp
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -cp /mp-includes:/app.war org.springframework.boot.loader.WarLauncher
