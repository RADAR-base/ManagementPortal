# Build stage
FROM openjdk:8-jdk as builder

# install node
RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - && \
    apt-get install -yq nodejs build-essential && \
    npm install -g npm yarn

# installing the node packages before adding the src directory will allow us to re-use these image layers when only the souce code changes
WORKDIR /app
COPY build.gradle gradle.properties gradlew package.json postcss.config.js proxy.conf.json settings.gradle tsconfig-aot.json tsconfig.json tslint.json yarn.lock /app/
COPY gradle gradle
COPY webpack webpack
COPY radar-auth radar-auth
COPY oauth-client-util oauth-client-util
RUN ./gradlew --no-daemon -s -Pprod npmInstall

# now we copy our application source code and build it
COPY src src
RUN ./gradlew --no-daemon -s -Pprod bootRepackage

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
